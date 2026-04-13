/**
 * 积分商城后端实现 - Controller 和异常处理部分
 */

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
            
            List<IntegralProduct> productList = 
                integralMallService.getProductList(productName);
            
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
            Integer userId = Integer.parseInt(params.get("userId").toString());
            Integer productId = Integer.parseInt(params.get("productId").toString());
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
            Integer pageNum = Integer.parseInt(params.get("pageNum").toString());
            Integer pageSize = Integer.parseInt(params.get("pageSize").toString());
            
            IPage<IntegralProduct> page = 
                integralMallService.adminGetProductList(productName, pageNum, pageSize);
            
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
    public Result addProduct(@RequestBody IntegralProduct product) {
        try {
            // 从session或token中获取当前用户ID
            Integer creatorId = getCurrentUserId();
            
            integralMallService.addProduct(product, creatorId);
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
    public Result updateProduct(@RequestBody IntegralProduct product) {
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
            Integer productId = Integer.parseInt(params.get("id").toString());
            integralMallService.deleteProduct(productId);
            return Result.success("删除成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败");
        }
    }
    
    /**
     * 获取用户的兑换记录
     */
    @PostMapping("/GetUserExchangeRecords")
    public Result getUserExchangeRecords(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = Integer.parseInt(params.get("userId").toString());
            
            List<IntegralExchange> records = 
                integralMallService.getUserExchangeRecords(userId);
            
            return Result.success(records);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取待发货的订单（后台）
     */
    @PostMapping("/GetPendingShipments")
    public Result getPendingShipments() {
        try {
            List<IntegralExchange> shipments = 
                integralMallService.getPendingShipments();
            
            return Result.success(shipments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 发货
     */
    @PostMapping("/ShipOrder")
    public Result shipOrder(@RequestBody Map<String, Object> params) {
        try {
            Integer exchangeId = Integer.parseInt(params.get("exchangeId").toString());
            String trackingNumber = (String) params.get("trackingNumber");
            String remark = (String) params.get("remark");
            Integer shippedBy = getCurrentUserId();
            
            integralMallService.shipOrder(exchangeId, trackingNumber, remark, shippedBy);
            
            return Result.success("发货成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("发货失败");
        }
    }
    
    /**
     * 确认收货
     */
    @PostMapping("/ConfirmReceipt")
    public Result confirmReceipt(@RequestBody Map<String, Object> params) {
        try {
            Integer exchangeId = Integer.parseInt(params.get("exchangeId").toString());
            
            integralMallService.confirmReceipt(exchangeId);
            
            return Result.success("确认收货成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("确认收货失败");
        }
    }
    
    /**
     * 获取当前用户ID（从session或token中获取）
     */
    private Integer getCurrentUserId() {
        // 这里需要根据你的项目实际情况获取当前用户ID
        // 可以从session、token或其他方式获取
        // 示例：
        // HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        // return (Integer) request.getSession().getAttribute("userId");
        return null;
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
@Data
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
}

// ==================== 关键修改说明 ====================

/**
 * 与原始版本的主要区别：
 * 
 * 1. 数据库表名修改：
 *    - integral_mall_product → integral_product
 *    - integral_mall_exchange_record → integral_exchange
 * 
 * 2. 字段名修改：
 *    - productName → productName (保持不变)
 *    - integralPrice → requiredIntegral
 *    - description → productDescription
 *    - imageUrl → imageUrl (保持不变)
 *    - status → status (1=上架, 2=下架)
 *    - 新增: sortOrder (排序字段)
 *    - 新增: creatorId (创建人)
 *    - 新增: creationTime (创建时间)
 * 
 * 3. 兑换记录表字段修改：
 *    - exchangeTime → creationTime
 *    - exchangeStatus → exchangeStatus (1=待发货, 2=已发货, 3=已收货)
 *    - 新增: shippingTime (发货时间)
 *    - 新增: shippedBy (发货人)
 *    - 新增: receivedTime (收货时间)
 *    - 新增: trackingNumber (物流单号)
 *    - 新增: shippingRemark (发货备注)
 * 
 * 4. 新增功能：
 *    - shipOrder() - 发货功能
 *    - confirmReceipt() - 确认收货功能
 *    - getPendingShipments() - 获取待发货订单
 * 
 * 5. 使用 MyBatis-Plus 的 BaseMapper 和 QueryWrapper
 *    确保与你的项目框架一致
 */
