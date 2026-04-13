<template>
  <div class="app-container">
    <el-page-header class="card margin-top-lg" @back="goBack" content="积分商城">
    </el-page-header>

    <!-- 用户积分信息 -->
    <el-card class="box-card margin-top-lg">
      <div class="integral-info">
        <span class="info-label">我的积分：</span>
        <span class="info-value">{{ userIntegral }}</span>
      </div>
    </el-card>

    <!-- 搜索和筛选 -->
    <el-card class="box-card margin-top-lg">
      <div slot="header" class="clearfix">
        <span>商品列表</span>
      </div>
      <div class="search-form">
        <el-form :inline="true" label-width="100px">
          <el-form-item label="商品名称">
            <el-input v-model.trim="searchForm.productNameLike" placeholder="请输入商品名称" :clearable="true"></el-input>
          </el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form>
      </div>
    </el-card>

    <!-- 商品展示 -->
    <el-card class="box-card margin-top-lg">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated></el-skeleton>
      </div>
      <div v-else-if="productList.length === 0" class="empty-container">
        <p>暂无商品</p>
      </div>
      <div v-else class="product-grid">
        <div v-for="product in productList" :key="product.Id" class="product-card">
          <div class="product-image">
            <img :src="product.ProductImage || require('@/assets/logo.png')" :alt="product.ProductName">
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.ProductName }}</h3>
            <p class="product-desc">{{ product.ProductDescription }}</p>
            <div class="product-footer">
              <span class="product-integral">{{ product.RequiredIntegral }} 积分</span>
              <el-button 
                type="primary" 
                size="small"
                :disabled="userIntegral < product.RequiredIntegral || product.Stock <= 0"
                @click="handleBuy(product)"
              >
                {{ product.Stock <= 0 ? '库存不足' : (userIntegral < product.RequiredIntegral ? '积分不足' : '兑换') }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 购买确认对话框 -->
    <el-dialog title="确认兑换" :visible.sync="buyDialogVisible" width="400px">
      <div v-if="selectedProduct" class="buy-dialog-content">
        <p><strong>商品名称：</strong>{{ selectedProduct.ProductName }}</p>
        <p><strong>所需积分：</strong>{{ selectedProduct.RequiredIntegral }}</p>
        <p><strong>当前积分：</strong>{{ userIntegral }}</p>
        <p><strong>兑换后积分：</strong>{{ userIntegral - selectedProduct.RequiredIntegral }}</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="buyDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmBuy" :loading="buyLoading">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'IntegralMall',
  computed: {
    ...mapGetters(['UserId'])
  },
  data() {
    return {
      userIntegral: 0,
      productList: [],
      loading: false,
      buyLoading: false,
      buyDialogVisible: false,
      selectedProduct: null,
      searchForm: {
        productNameLike: ''
      }
    }
  },
  created() {
    this.getMyIntegral()
    this.getProductList()
  },
  methods: {
    goBack() {
      this.$router.go(-1)
    },
    async getMyIntegral() {
      try {
        const { Data } = await this.$Post('/Integral/GetMyIntegralData', {})
        this.userIntegral = Data.TotalIntegral || 0
      } catch (error) {
        this.$message.error('获取积分失败')
      }
    },
    async getProductList() {
      this.loading = true
      try {
        const params = {
          productNameLike: this.searchForm.productNameLike,
          page: 1,
          limit: 100
        }
        const { Data } = await this.$Post('/IntegralMall/GetAvailableProducts', params)
        this.productList = Data.Items || []
      } catch (error) {
        this.$message.error('获取商品列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.getProductList()
    },
    handleReset() {
      this.searchForm = {
        productNameLike: ''
      }
      this.getProductList()
    },
    handleBuy(product) {
      if (this.userIntegral < product.RequiredIntegral) {
        this.$message.warning('积分不足，无法兑换')
        return
      }
      if (product.Stock <= 0) {
        this.$message.warning('库存不足，无法兑换')
        return
      }
      this.selectedProduct = product
      this.buyDialogVisible = true
    },
    async confirmBuy() {
      if (!this.selectedProduct) return
      
      this.buyLoading = true
      try {
        const params = {
          ProductId: this.selectedProduct.Id,
          DeliveryAddress: '',
          RecipientName: '',
          RecipientPhone: ''
        }
        console.log('兑换参数:', params)
        await this.$Post('/IntegralOrder/CreateOrder', params)
        this.$message.success('兑换成功！')
        this.buyDialogVisible = false
        this.selectedProduct = null
        this.getMyIntegral()
        this.getProductList()
      } catch (error) {
        console.error('兑换失败:', error)
        this.$message.error(error.message || '兑换失败')
      } finally {
        this.buyLoading = false
      }
    }
  }
}
</script>

<style scoped lang="css">
.app-container {
  padding: 20px;
}

.integral-info {
  display: flex;
  align-items: center;
  font-size: 18px;
  padding: 20px;
}

.info-label {
  font-weight: bold;
  margin-right: 10px;
}

.info-value {
  color: #ff6b6b;
  font-size: 24px;
  font-weight: bold;
}

.search-form {
  padding: 10px 0;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  padding: 10px;
}

.product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  margin: 0 0 12px 0;
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.product-integral {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
}

.buy-dialog-content {
  padding: 20px 0;
}

.buy-dialog-content p {
  margin: 12px 0;
  font-size: 14px;
}

.margin-top-lg {
  margin-top: 20px;
}

.box-card {
  margin-bottom: 20px;
}
</style>
