/**
 * 积分商城相关API
 */

// 获取上架商品列表（前台）
export function getAvailableProducts(params) {
  return this.$Post('/IntegralMall/GetAvailableProducts', params)
}

// 创建订单（兑换商品）
export function createOrder(params) {
  return this.$Post('/IntegralOrder/CreateOrder', params)
}

// 获取我的积分
export function getMyIntegral() {
  return this.$Post('/Integral/GetMyIntegralData', {})
}

// 获取商品列表（后台管理）
export function getProductList(params) {
  return this.$Post('/IntegralMall/List', params)
}

// 新增或编辑商品
export function createOrEditProduct(params) {
  return this.$Post('/IntegralMall/CreateOrEdit', params)
}

// 删除商品
export function deleteProduct(params) {
  return this.$Post('/IntegralMall/Delete', params)
}

// 获取我的订单
export function getMyOrders(params) {
  return this.$Post('/IntegralOrder/GetMyOrders', params)
}

// 更新订单状态
export function updateOrderStatus(params, status) {
  return this.$Post('/IntegralOrder/UpdateOrderStatus', { ...params, status })
}
