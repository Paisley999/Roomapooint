/**
 * 积分商城后端实现参考
 * 这是一个Java实现示例，展示如何实现积分商城的后端逻辑
 */

// ==================== Entity 实体类 ====================

/**
 * 商品实体类
 */
public class IntegralMallProduct {
    private Long id;
    private String productName;
    private String category;
    private Integer integralPrice;
    private Integer stock;
    private String description;
    private String imageUrl;
    private Integer status; // 1:上架, 0:下架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // getter/setter...
}

/**
 * 兑换记录实体类
 */
public class IntegralMallExchangeRecord {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private Integer integralPrice;
    private Integer quantity;
    private LocalDateTime exchangeTime;
    private Integer status; // 1:成功, 0:失败
    private String remark;
    
    // getter/setter...
}

// ==================== Mapper 数据访问层 ====================

@Mapper
public interface IntegralMallProductMapper extends BaseMapper<IntegralMallProduct> {
    /**
     * 根据分类查询商品
     */
    List<IntegralMallProduct> selectByCategory(@Param("category") String category);
    
    /**
     * 模糊查询商品
     */
    List<IntegralMallProduct> selectByProductName(@Param("productName") String productName);
}

@Mapper
public interface IntegralMallExchangeRecordMapper extends BaseMapper<IntegralMallExchangeRecord> {
    /**
     * 查询用户的兑换记录
     */
    List<IntegralMallExchangeRecord> selectByUserId(@Param("userId") Long userId);
}

// ==================== Service 业务逻辑层 ====================

@Service
public class IntegralMallService {
    
    @Autowired
    private IntegralMallProductMapper productMapper;
    
    @Autowired
    private IntegralMallExchangeRecordMapper exchangeRecordMapper;
    
    @Autowired
    private IntegralService integralService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取商品列表（前台）
     */
    public List<IntegralMallProduct> getProductList(String productName, String category) {
        QueryWrapper<IntegralMallProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 只显示上架商品
        
        if (StringUtils.isNotBlank(productName)) {
            queryWrapper.like("product_name", productName);
        }
        
        if (StringUtils.isNotBlank(category)) {
            queryWrapper.eq("category", category);
        }
        
        return productMapper.selectList(queryWrapper);
    }
    
    /**
     * 获取商品列表（后台管理，分页）
     */
    public IPage<IntegralMallProduct> adminGetProductList(String productName, String category, 
                                                          Integer pageNum, Integer pageSize) {
        Page<IntegralMallProduct> page = new Page<>(pageNum, pageSize);
        QueryWrapper<IntegralMallProduct> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.isNotBlank(productName)) {
            queryWrapper.like("product_name", productName);
        }
        
        if (StringUtils.isNotBlank(category)) {
            queryWrapper.eq("category", category);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        return productMapper.selectPage(page, queryWrapper);
    }
    
    /**
     * 新增商品
     */
    @Transactional
    public void addProduct(IntegralMallProduct product) {
        if (product == null || StringUtils.isBlank(product.getProductName())) {
            throw new BusinessException("商品名称不能为空");
        }
        
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        
        int result = productMapper.insert(product);
        if (result <= 0) {
            throw new BusinessException("新增商品失败");
        }
    }
    
    /**
     * 编辑商品
     */
    @Transactional
    public void updateProduct(IntegralMallProduct product) {
        if (product == null || product.getId() == null) {
            throw new BusinessException("商品ID不能为空");
        }
        
        product.setUpdateTime(LocalDateTime.now());
        
        int result = productMapper.updateById(product);
        if (result <= 0) {
            throw new BusinessException("编辑商品失败");
        }
    }
    
    /**
     * 删除商品
     */
    @Transactional
    public void deleteProduct(Long productId) {
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
    public void buyProduct(Long userId, Long productId, Integer quantity) {
        // 1. 验证参数
        if (userId == null || productId == null || quantity == null || quantity <= 0) {
            throw new BusinessException("参数错误");
        }
        
        // 2. 查询商品
        IntegralMallProduct product = productMapper.selectById(productId);
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
        Integer totalIntegral = product.getIntegralPrice() * quantity;
        
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
        IntegralMallExchangeRecord record = new IntegralMallExchangeRecord();
        record.setUserId(userId);
        record.setProductId(productId);
        record.setProductName(product.getProductName());
        record.setIntegralPrice(product.getIntegralPrice());
        record.setQuantity(quantity);
        record.setExchangeTime(LocalDateTime.now());
        record.setStatus(1); // 成功
        
        exchangeRecordMapper.insert(record);
    }
    
    /**
     * 获取用户的兑换记录
     */
    public List<IntegralMallExchangeRecord> getUserExchangeRecords(Long userId) {
        return exchangeRecordMapper.selectByUserId(userId);
    }
}

// ==================== Controller 控制层 ====================

@RestController
@RequestMapping("/IntegralMall")
public class IntegralMallController {
    
    @Autowired
    private IntegralMallService integralMallService;
    
    /**
     * 获取商品列表（前台）
     */
    @PostMapping("/GetProductList")
    public Result getProductList(@RequestBody Map<String, String> params) {
        try {
            String productName = params.get("productName");
            String category = params.get("category");
            
            List<IntegralMallProduct> productList = 
                integralMallService.getProductList(productName, category);
            
            return Result.success(productList);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 购买商品
     */
    @PostMapping("/BuyProduct")
    public Result buyProduct(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.parseLong(params.get("userId").toString());
            Long productId = Long.parseLong(params.get("productId").toString());
            Integer quantity = Integer.parseInt(params.get("quantity").toString());
            
            integralMallService.buyProduct(userId, productId, quantity);
            
            return Result.success("兑换成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("兑换失败");
        }
    }
    
    /**
     * 获取商品列表（后台管理）
     */
    @PostMapping("/AdminGetProductList")
    public Result adminGetProductList(@RequestBody Map<String, Object> params) {
        try {
            String productName = (String) params.get("productName");
            String category = (String) params.get("category");
            Integer pageNum = Integer.parseInt(params.get("pageNum").toString());
            Integer pageSize = Integer.parseInt(params.get("pageSize").toString());
            
            IPage<IntegralMallProduct> page = 
                integralMallService.adminGetProductList(productName, category, pageNum, pageSize);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", page.getRecords());
            result.put("total", page.getTotal());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 新增商品
     */
    @PostMapping("/AddProduct")
    public Result addProduct(@RequestBody IntegralMallProduct product) {
        try {
            integralMallService.addProduct(product);
            return Result.success("新增成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("新增失败");
        }
    }
    
    /**
     * 编辑商品
     */
    @PostMapping("/UpdateProduct")
    public Result updateProduct(@RequestBody IntegralMallProduct product) {
        try {
            integralMallService.updateProduct(product);
            return Result.success("编辑成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("编辑失败");
        }
    }
    
    /**
     * 删除商品
     */
    @PostMapping("/DeleteProduct")
    public Result deleteProduct(@RequestBody Map<String, Object> params) {
        try {
            Long productId = Long.parseLong(params.get("id").toString());
            integralMallService.deleteProduct(productId);
            return Result.success("删除成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败");
        }
    }
}

// ==================== 异常处理 ====================

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

/**
 * 统一返回结果
 */
public class Result {
    private Integer code;
    private String message;
    private Object data;
    
    public static Result success(Object data) {
        Result result = new Result();
        result.code = 200;
        result.message = "成功";
        result.data = data;
        return result;
    }
    
    public static Result error(String message) {
        Result result = new Result();
        result.code = 500;
        result.message = message;
        return result;
    }
    
    // getter/setter...
}
