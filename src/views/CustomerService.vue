<template>
  <div class="customer-service-container">
    <!-- 客服列表 -->
    <div class="service-list-panel">
      <h3>在线客服</h3>
      <div v-if="serviceList.length === 0" class="empty-state">
        <p>暂无在线客服</p>
      </div>
      <div v-else class="service-items">
        <div
          v-for="service in serviceList"
          :key="service.id"
          class="service-item"
          :class="{ active: selectedService && selectedService.id === service.id }"
          @click="selectService(service)"
        >
          <img :src="service.avatar" :alt="service.serviceName" class="avatar" />
          <div class="service-info">
            <p class="name">{{ service.serviceName }}</p>
            <p class="status" :class="{ online: service.status === 1 }">
              {{ service.status === 1 ? '在线' : '离线' }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 聊天窗口 -->
    <div class="chat-panel" v-if="selectedService">
      <div class="chat-header">
        <h3>{{ selectedService.serviceName }}</h3>
        <button @click="closeChat" class="close-btn">×</button>
      </div>

      <div class="chat-messages" ref="messagesContainer">
        <div
          v-for="message in chatMessages"
          :key="message.id"
          class="message"
          :class="{ 'user-message': message.messageType === 0, 'service-message': message.messageType === 1 }"
        >
          <div class="message-content">{{ message.messageContent }}</div>
          <div class="message-time">{{ formatTime(message.creationTime) }}</div>
        </div>
      </div>

      <div class="chat-input-area">
        <textarea
          v-model="inputMessage"
          placeholder="输入消息..."
          @keydown.enter.ctrl="sendMessage"
          class="message-input"
        ></textarea>
        <button @click="sendMessage" class="send-btn">发送</button>
      </div>
    </div>

    <!-- 管理面板（仅管理员） -->
    <div v-if="isAdmin" class="admin-panel">
      <h3>客服管理</h3>
      <el-button type="primary" @click="showAddServiceDialog">添加客服</el-button>
      <el-table :data="adminServiceList" style="margin-top: 20px">
        <el-table-column prop="serviceName" label="客服名称"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template slot-scope="scope">
            <span :class="{ 'status-online': scope.row.status === 1 }">
              {{ scope.row.status === 1 ? '在线' : '离线' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用">
          <template slot-scope="scope">
            {{ scope.row.enabled === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="small" @click="editService(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteService(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑客服对话框 -->
    <el-dialog title="客服信息" :visible.sync="serviceDialogVisible" width="500px">
      <el-form :model="serviceForm" label-width="100px">
        <el-form-item label="客服名称">
          <el-input v-model="serviceForm.serviceName"></el-input>
        </el-form-item>
        <el-form-item label="客服描述">
          <el-input v-model="serviceForm.serviceDescription" type="textarea"></el-input>
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="serviceForm.avatar"></el-input>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="serviceForm.enabled" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="serviceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveService">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getEnabledServices, getChatHistory, sendMessage, getCustomerServiceList, createOrEditCustomerService, deleteCustomerService } from '@/api/customerService'
import { mapGetters } from 'vuex'

export default {
  name: 'CustomerService',
  data() {
    return {
      serviceList: [],
      adminServiceList: [],
      selectedService: null,
      chatMessages: [],
      inputMessage: '',
      serviceDialogVisible: false,
      serviceForm: {
        serviceName: '',
        serviceDescription: '',
        avatar: '',
        enabled: 1
      },
      editingServiceId: null
    }
  },
  computed: {
    ...mapGetters(['RoleType']),
    isAdmin() {
      return this.RoleType === '管理员'
    }
  },
  mounted() {
    this.loadServices()
    if (this.isAdmin) {
      this.loadAdminServices()
    }
  },
  methods: {
    // 加载启用的客服列表
    loadServices() {
      getEnabledServices({ page: 1, limit: 100 }).then(res => {
        this.serviceList = res.data.items || []
      }).catch(err => {
        this.$message.error('加载客服列表失败')
      })
    },

    // 加载管理员客服列表
    loadAdminServices() {
      getCustomerServiceList({ page: 1, limit: 100 }).then(res => {
        this.adminServiceList = res.data.items || []
      }).catch(err => {
        this.$message.error('加载客服列表失败')
      })
    },

    // 选择客服
    selectService(service) {
      this.selectedService = service
      this.loadChatHistory()
    },

    // 加载聊天记录
    loadChatHistory() {
      if (!this.selectedService) return
      getChatHistory(this.$store.getters.UserId, this.selectedService.id, 1, 50).then(res => {
        this.chatMessages = res.data.items || []
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }).catch(err => {
        this.$message.error('加载聊天记录失败')
      })
    },

    // 发送消息
    sendMessage() {
      if (!this.inputMessage.trim()) {
        this.$message.warning('请输入消息内容')
        return
      }

      const messageData = {
        serviceId: this.selectedService.id,
        messageContent: this.inputMessage
      }

      sendMessage(messageData).then(res => {
        this.chatMessages.push(res.data)
        this.inputMessage = ''
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }).catch(err => {
        this.$message.error('发送消息失败')
      })
    },

    // 关闭聊天
    closeChat() {
      this.selectedService = null
      this.chatMessages = []
      this.inputMessage = ''
    },

    // 显示添加客服对话框
    showAddServiceDialog() {
      this.editingServiceId = null
      this.serviceForm = {
        serviceName: '',
        serviceDescription: '',
        avatar: '',
        enabled: 1
      }
      this.serviceDialogVisible = true
    },

    // 编辑客服
    editService(service) {
      this.editingServiceId = service.id
      this.serviceForm = { ...service }
      this.serviceDialogVisible = true
    },

    // 保存客服
    saveService() {
      if (!this.serviceForm.serviceName) {
        this.$message.warning('请输入客服名称')
        return
      }

      const data = { ...this.serviceForm }
      if (this.editingServiceId) {
        data.id = this.editingServiceId
      }

      createOrEditCustomerService(data).then(res => {
        this.$message.success('保存成功')
        this.serviceDialogVisible = false
        this.loadAdminServices()
        this.loadServices()
      }).catch(err => {
        this.$message.error('保存失败')
      })
    },

    // 删除客服
    deleteService(service) {
      this.$confirm('确定删除该客服吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCustomerService({ id: service.id }).then(res => {
          this.$message.success('删除成功')
          this.loadAdminServices()
          this.loadServices()
        }).catch(err => {
          this.$message.error('删除失败')
        })
      }).catch(() => {})
    },

    // 滚动到底部
    scrollToBottom() {
      const container = this.$refs.messagesContainer
      if (container) {
        container.scrollTop = container.scrollHeight
      }
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
  }
}
</script>

<style scoped lang="scss">
.customer-service-container {
  display: flex;
  gap: 20px;
  padding: 20px;
  height: 100%;

  .service-list-panel {
    flex: 0 0 250px;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 15px;
    overflow-y: auto;

    h3 {
      margin: 0 0 15px 0;
      font-size: 16px;
      color: #333;
    }

    .empty-state {
      text-align: center;
      color: #999;
      padding: 20px 0;
    }

    .service-items {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .service-item {
      display: flex;
      gap: 10px;
      padding: 10px;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background-color: #f5f5f5;
      }

      &.active {
        background-color: #e6f7ff;
        border-left: 3px solid #1890ff;
      }

      .avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        object-fit: cover;
      }

      .service-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: center;

        .name {
          margin: 0;
          font-size: 14px;
          font-weight: 500;
          color: #333;
        }

        .status {
          margin: 0;
          font-size: 12px;
          color: #999;

          &.online {
            color: #52c41a;
          }
        }
      }
    }
  }

  .chat-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    border: 1px solid #ddd;
    border-radius: 4px;
    overflow: hidden;

    .chat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      border-bottom: 1px solid #ddd;
      background-color: #fafafa;

      h3 {
        margin: 0;
        font-size: 16px;
        color: #333;
      }

      .close-btn {
        background: none;
        border: none;
        font-size: 24px;
        cursor: pointer;
        color: #999;

        &:hover {
          color: #333;
        }
      }
    }

    .chat-messages {
      flex: 1;
      overflow-y: auto;
      padding: 15px;
      display: flex;
      flex-direction: column;
      gap: 10px;

      .message {
        display: flex;
        flex-direction: column;
        max-width: 70%;

        &.user-message {
          align-self: flex-end;

          .message-content {
            background-color: #1890ff;
            color: white;
          }
        }

        &.service-message {
          align-self: flex-start;

          .message-content {
            background-color: #f0f0f0;
            color: #333;
          }
        }

        .message-content {
          padding: 10px 15px;
          border-radius: 4px;
          word-wrap: break-word;
          font-size: 14px;
        }

        .message-time {
          font-size: 12px;
          color: #999;
          margin-top: 5px;
          padding: 0 5px;
        }
      }
    }

    .chat-input-area {
      display: flex;
      gap: 10px;
      padding: 15px;
      border-top: 1px solid #ddd;
      background-color: #fafafa;

      .message-input {
        flex: 1;
        padding: 10px;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 14px;
        font-family: inherit;
        resize: none;
        max-height: 100px;

        &:focus {
          outline: none;
          border-color: #1890ff;
        }
      }

      .send-btn {
        padding: 10px 20px;
        background-color: #1890ff;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 14px;

        &:hover {
          background-color: #40a9ff;
        }
      }
    }
  }

  .admin-panel {
    flex: 1;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 20px;
    overflow-y: auto;

    h3 {
      margin: 0 0 15px 0;
      font-size: 16px;
      color: #333;
    }

    .status-online {
      color: #52c41a;
      font-weight: 500;
    }
  }
}

@media (max-width: 768px) {
  .customer-service-container {
    flex-direction: column;

    .service-list-panel {
      flex: 0 0 auto;
      max-height: 200px;
    }

    .chat-panel,
    .admin-panel {
      flex: 1;
    }
  }
}
</style>
