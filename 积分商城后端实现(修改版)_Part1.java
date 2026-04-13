/**
 * 积分商城后端实现 - 根据实际数据库结构修改版
 * 数据库表：integral_product（商品表）、integral_exchange（兑换记录表）
 */

// ==================== Entity 实体类 ====================

/**
 * 商品实体类 - 对应 integral_product 表
 */
@Data
@TableName("integral_product")
public class IntegralProduct {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private LocalDateTime creationTime;
    private Integer creatorId;
    private String productName;
    private String productDescription;
    private Integer requiredIntegral;
    private Integer stock;
    private String imageUrl;
    private Integer status; // 1=上架 2=下架
    private Integer sortOrder;
}

/**
 * 兑换记录实体类 - 对应 integral_exchange 表
 */
@Data
@TableName("integral_exchange")
public class IntegralExchange {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private LocalDateTime creationTime;
    private Integer userId;
    private Integer productId;
    private Integer exchangeIntegral;
    private Integer exchangeStatus; // 1=待发货 2=已发货 3=已收货
    private LocalDateTime shippingTime;
    private Integer shippedBy;
    private LocalDateTime receivedTime;
    private String trackingNumber;
    private String shippingRemark;
}

// ==================== Mapper 数据访问层 ====================

@Mapper
public interface IntegralProductMapper extends BaseMapper<IntegralProduct> {
    /**
     * 查询上架的商品列表
     */
    @Select("SELECT * FROM integral_product WHERE status = 1 ORDER BY sort_order ASC, creation_time DESC")
    List<IntegralProduct> selectOnSaleProducts();
    
    /**
     * 模糊查询商品
     */
    @Select("SELECT * FROM integral_product WHERE product_name LIKE CONCAT('%', #{productName}, '%') ORDER BY sort_order ASC")
    List<IntegralProduct> selectByProductName(@Param("productName") String productName);
}

@Mapper
public interface IntegralExchangeMapper extends BaseMapper<IntegralExchange> {
    /**
     * 查询用户的兑换记录
     */
    @Select("SELECT * FROM integral_exchange WHERE user_id = #{userId} ORDER BY creation_time DESC")
    List<IntegralExchange> selectByUserId(@Param("userId") Integer userId);
    
    /**
     * 查询待发货的订单
     */
    @Select("SELECT * FROM integral_exchange WHERE exchange_status = 1 ORDER BY creation_time ASC")
    List<IntegralExchange> selectPendingShipments();
}

// ==================== Service 业务逻辑层 ====================

@Service
public class IntegralMallService {
    
    @Autowired
    private IntegralProductMapper productMapper;
    
    @Autowired
    private IntegralExchangeMapper exchangeMapper;
    
    @Autowired
    private IntegralService integralService;
    
    /**
     * 获取商品列表（前台）- 只显示上架商品
     */
    public List<IntegralProduct> getProductList(String productName) {
        if (StringUtils.isNotBlank(productName)) {
            return productMapper.selectByProductName(productName);
        }
        return productMapper.selectOnSaleProducts();
    }
    
    /**
     * 获取商品列表（后台管理，分页）
     */
    public IPage<IntegralProduct> adminGetProductList(String productName, Integer pageNum, Integer pageSize) {
        Page<IntegralProduct> page = new Page<>(pageNum, pageSize);
        QueryWrapper<IntegralProduct> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.isNotBlank(productName)) {
            queryWrapper.like("product_name", productName);
        }
        
        queryWrapper.orderByAsc("sort_order").orderByDesc("creation_time");
        
        return productMapper.selectPage(page, queryWrapper);
    }
    
    /**
     * 新增商品
     */
    @Transactional
    public void addProduct(IntegralProduct product, Integer creatorId) {
        if (product == null || StringUtils.isBlank(product.getProductName())) {
            throw new BusinessException("商品名称不能为空");
        }
        
        if (product.getRequiredIntegral() == null || product.getRequiredIntegral() <= 0) {
            throw new BusinessException("所需积分必须大于0");
        }
        
        product.setCreationTime(LocalDateTime.now());
        product.setCreatorId(creatorId);
        product.setStatus(1); // 默认上架
        
        int result = productMapper.insert(product);
        if (result <= 0) {
            throw new BusinessException("新增商品失败");
        }
    }
    
    /**
     * 编辑商品
     */
    @Transactional
    public void updateProduct(IntegralProduct product) {
        if (product == null || product.getId() == null) {
            throw new BusinessException("商品ID不能为空");
        }
        
        IntegralProduct existProduct = productMapper.selectById(product.getId());
        if (existProduct == null) {
            throw new BusinessException("商品不存在");
        }
        
        int result = productMapper.updateById(product);
        if (result <= 0) {
            throw new BusinessException("编辑商品失败");
        }
    }
    
    /**
     * 删除商品
     */
    @Transactional
    public void deleteProduct(Integer productId) {
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        
        int result = productMapper.deleteById(productId);
        if (result <= 0) {
            throw new BusinessException("删除商品失败");
        }
    }
    
    /**
     * 购买商品（兑换）
     */
    @Transactional
    public void buyProduct(Integer userId, Integer productId, Integer quantity) {
        // 1. 验证参数
        if (userId == null || productId == null || quantity == null || quantity <= 0) {
            throw new BusinessException("参数错误");
        }
        
        // 2. 查询商品
        IntegralProduct product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 3. 检查商品状态
        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }
        
        // 4. 检查库存
        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }
        
        // 5. 计算所需积分
        Integer totalIntegral = product.getRequiredIntegral() * quantity;
        
        // 6. 查询用户积分
        IntegralData userIntegral = integralService.getUserIntegral(userId);
        if (userIntegral.getTotalIntegral() < totalIntegral) {
            throw new BusinessException("积分不足");
        }
        
        // 7. 扣除用户积分
        integralService.deductIntegral(userId, totalIntegral, 
            "兑换商品：" + product.getProductName(), String.valueOf(productId));
        
        // 8. 更新商品库存
        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);
        
        // 9. 记录兑换历史
        IntegralExchange exchange = new IntegralExchange();
        exchange.setUserId(userId);
        exchange.setProductId(productId);
        exchange.setExchangeIntegral(totalIntegral);
        exchange.setCreationTime(LocalDateTime.now());
        exchange.setExchangeStatus(1); // 待发货
        
        exchangeMapper.insert(exchange);
    }
    
    /**
     * 获取用户的兑换记录
     */
    public List<IntegralExchange> getUserExchangeRecords(Integer userId) {
        return exchangeMapper.selectByUserId(userId);
    }
    
    /**
     * 获取待发货的订单
     */
    public List<IntegralExchange> getPendingShipments() {
        return exchangeMapper.selectPendingShipments();
    }
    
    /**
     * 发货
     */
    @Transactional
    public void shipOrder(Integer exchangeId, String trackingNumber, String remark, Integer shippedBy) {
        if (exchangeId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        
        IntegralExchange exchange = exchangeMapper.selectById(exchangeId);
        if (exchange == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (exchange.getExchangeStatus() != 1) {
            throw new BusinessException("订单状态不正确，无法发货");
        }
        
        exchange.setExchangeStatus(2); // 已发货
        exchange.setShippingTime(LocalDateTime.now());
        exchange.setTrackingNumber(trackingNumber);
        exchange.setShippingRemark(remark);
        exchange.setShippedBy(shippedBy);
        
        int result = exchangeMapper.updateById(exchange);
        if (result <= 0) {
            throw new BusinessException("发货失败");
        }
    }
    
    /**
     * 确认收货
     */
    @Transactional
    public void confirmReceipt(Integer exchangeId) {
        if (exchangeId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        
        IntegralExchange exchange = exchangeMapper.selectById(exchangeId);
        if (exchange == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (exchange.getExchangeStatus() != 2) {
            throw new BusinessException("订单状态不正确，无法确认收货");
        }
        
        exchange.setExchangeStatus(3); // 已收货
        exchange.setReceivedTime(LocalDateTime.now());
        
        int result = exchangeMapper.updateById(exchange);
        if (result <= 0) {
            throw new BusinessException("确认收货失败");
        }
    }
}
