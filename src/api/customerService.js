import http from './http'

/**
 * 客服相关API接口
 */

// 获取客服列表
export const getCustomerServiceList = (data) => {
  return http.post('/api/customerService/list', data)
}

// 获取启用的客服列表
export const getEnabledServices = (data) => {
  return http.post('/api/customerService/getEnabledServices', data)
}

// 获取单个客服
export const getCustomerService = (data) => {
  return http.post('/api/customerService/get', data)
}

// 创建或修改客服
export const createOrEditCustomerService = (data) => {
  return http.post('/api/customerService/createOrEdit', data)
}

// 删除客服
export const deleteCustomerService = (data) => {
  return http.post('/api/customerService/delete', data)
}

// 批量删除客服
export const batchDeleteCustomerService = (data) => {
  return http.post('/api/customerService/batchDelete', data)
}

// 发送消息
export const sendMessage = (data) => {
  return http.post('/api/customerMessage/sendMessage', data)
}

// 获取聊天记录
export const getChatHistory = (userId, serviceId, page = 1, limit = 10) => {
  return http.get('/api/customerMessage/getChatHistory', {
    params: { userId, serviceId, page, limit }
  })
}

// 获取消息列表
export const getMessageList = (data) => {
  return http.post('/api/customerMessage/list', data)
}
