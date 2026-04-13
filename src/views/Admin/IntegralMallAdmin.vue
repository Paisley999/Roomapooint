<template>
  <div class="app-container">
    <el-page-header class="card margin-top-lg" @back="goBack" content="积分商城管理">
    </el-page-header>

    <el-card class="box-card margin-top-lg">
      <div slot="header" class="clearfix">
        <el-button type="primary" size="mini" icon="el-icon-plus" @click="handleAdd">新增商品</el-button>
        <el-button type="warning" size="mini" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        <el-button type="info" size="mini" icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </div>

      <div class="search-form">
        <el-form :inline="true" label-width="100px">
          <el-form-item label="商品名称">
            <el-input v-model.trim="searchForm.productNameLike" placeholder="请输入商品名称" :clearable="true"></el-input>
          </el-form-item>
          <el-form-item label="商品状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" :clearable="true">
              <el-option label="全部" :value="null"></el-option>
              <el-option label="上架" :value="1"></el-option>
              <el-option label="下架" :value="0"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="box-card margin-top-lg">
      <div v-if="loading" style="text-align: center; padding: 40px;">
        <el-skeleton :rows="5" animated></el-skeleton>
      </div>
      <div v-else-if="productList.length === 0" style="text-align: center; padding: 40px; color: #999;">
        <p>暂无商品数据</p>
      </div>
      <div v-else>
        <el-table :data="productList" stripe style="width: 100%">
          <el-table-column prop="Id" label="ID" width="80"></el-table-column>
          <el-table-column prop="ProductName" label="商品名称" min-width="150"></el-table-column>
          <el-table-column prop="RequiredIntegral" label="所需积分" width="120"></el-table-column>
          <el-table-column prop="Stock" label="库存" width="100"></el-table-column>
          <el-table-column prop="ProductDescription" label="描述" min-width="200" show-overflow-tooltip></el-table-column>
          <el-table-column prop="Status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.Status === 1 ? 'success' : 'danger'">
                {{ scope.row.Status === 1 ? '上架' : '下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template slot-scope="scope">
              <el-button type="primary" size="mini" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          style="margin-top: 20px; text-align: right;"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="商品名称" prop="ProductName">
          <el-input v-model.trim="formData.ProductName" placeholder="请输入商品名称"></el-input>
        </el-form-item>
        <el-form-item label="所需积分" prop="RequiredIntegral">
          <el-input-number v-model="formData.RequiredIntegral" :min="1" :max="999999"></el-input-number>
        </el-form-item>
        <el-form-item label="库存" prop="Stock">
          <el-input-number v-model="formData.Stock" :min="0" :max="999999"></el-input-number>
        </el-form-item>
        <el-form-item label="商品描述" prop="ProductDescription">
          <el-input v-model.trim="formData.ProductDescription" type="textarea" rows="3" placeholder="请输入商品描述"></el-input>
        </el-form-item>
        <el-form-item label="商品图片" prop="ProductImage">
          <UploadImages v-model="formData.ProductImage" :limit="1"></UploadImages>
        </el-form-item>
        <el-form-item label="状态" prop="Status">
          <el-radio-group v-model="formData.Status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import UploadImages from '@/components/Upload/UploadImages.vue'

export default {
  name: 'IntegralMallAdmin',
  components: {
    UploadImages
  },
  data() {
    return {
      productList: [],
      loading: false,
      saveLoading: false,
      dialogVisible: false,
      dialogTitle: '新增商品',
      isEdit: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchForm: {
        productNameLike: '',
        status: null
      },
      formData: {
        Id: null,
        ProductName: '',
        RequiredIntegral: 100,
        Stock: 0,
        ProductDescription: '',
        ProductImage: '',
        Status: 1
      },
      formRules: {
        ProductName: [
          { required: true, message: '请输入商品名称', trigger: 'blur' }
        ],
        RequiredIntegral: [
          { required: true, message: '请输入所需积分', trigger: 'blur' }
        ],
        Stock: [
          { required: true, message: '请输入库存', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getProductList()
  },
  methods: {
    goBack() {
      this.$router.go(-1)
    },
    async getProductList() {
      this.loading = true
      try {
        const params = {
          productNameLike: this.searchForm.productNameLike || '',
          status: this.searchForm.status,
          page: this.pageNum,
          limit: this.pageSize
        }
        const response = await this.$Post('/IntegralMall/List', params)
        
        if (response && response.Data) {
          this.productList = response.Data.Items || []
          this.total = response.Data.Total || 0
        } else {
          this.productList = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取商品列表失败:', error)
        this.$message.error('获取商品列表失败')
        this.productList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.getProductList()
    },
    handleReset() {
      this.searchForm = {
        productNameLike: '',
        status: null
      }
      this.pageNum = 1
      this.getProductList()
    },
    handleAdd() {
      this.isEdit = false
      this.dialogTitle = '新增商品'
      this.formData = {
        Id: null,
        ProductName: '',
        RequiredIntegral: 100,
        Stock: 0,
        ProductDescription: '',
        ProductImage: '',
        Status: 1
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.formRef) {
          this.$refs.formRef.clearValidate()
        }
      })
    },
    handleEdit(row) {
      this.isEdit = true
      this.dialogTitle = '编辑商品'
      this.formData = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.formRef) {
          this.$refs.formRef.clearValidate()
        }
      })
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除商品"${row.ProductName}"吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await this.$Post('/IntegralMall/Delete', { Id: row.Id })
        this.$message.success('删除成功')
        this.getProductList()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    handleSave() {
      if (!this.$refs.formRef) return
      
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return
        
        this.saveLoading = true
        try {
          await this.$Post('/IntegralMall/CreateOrEdit', this.formData)
          this.$message.success(this.isEdit ? '编辑成功' : '新增成功')
          this.dialogVisible = false
          this.getProductList()
        } catch (error) {
          this.$message.error(error.message || '保存失败')
        } finally {
          this.saveLoading = false
        }
      })
    },
    handleDialogClose() {
      if (this.$refs.formRef) {
        this.$refs.formRef.clearValidate()
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
      this.getProductList()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getProductList()
    }
  }
}
</script>

<style scoped lang="css">
.app-container {
  padding: 20px;
}

.search-form {
  padding: 10px 0;
}

.margin-top-lg {
  margin-top: 20px;
}

.box-card {
  margin-bottom: 20px;
}
</style>
