<template>
    <div class="app-container">
        <el-card class="box-card">
            <div slot="header" class="clearfix">
                <el-button type="primary" size="mini" icon="el-icon-search" @click="SearchClick">搜索</el-button>
                <el-button type="warning" size="mini" @click="ResetClick" icon="el-icon-refresh">重置</el-button>
            </div>
            <div class="tb-body">
                <el-form ref="searchFormRef" :model="searchForm" :inline="true" label-width="80px">
                    <el-form-item label="标题">
                        <el-input v-model.trim="searchForm.TitleLike" placeholder="标题" clearable></el-input>
                    </el-form-item>
                    <el-form-item label="类型">
                        <el-select v-model="searchForm.ItemType" placeholder="全部" clearable style="width:110px">
                            <el-option label="失物" :value="1"></el-option>
                            <el-option label="招领" :value="2"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="状态">
                        <el-select v-model="searchForm.ItemStatus" placeholder="全部" clearable style="width:110px">
                            <el-option label="待处理" :value="1"></el-option>
                            <el-option label="已处理" :value="2"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="发布人">
                        <el-input v-model.trim="searchForm.PublisherNameLike" placeholder="发布人" clearable></el-input>
                    </el-form-item>
                </el-form>
            </div>
        </el-card>
        <!-- 详情弹窗 -->
        <el-dialog title="失物招领详情" :visible.sync="detailShow" width="500px">
            <div style="margin-bottom:10px">
                <el-tag :type="detailData.ItemType===1?'danger':'success'" size="medium">{{ detailData.ItemType===1?'失物':'招领' }}</el-tag>
                <el-tag :type="detailData.ItemStatus===1?'warning':'info'" size="medium" style="margin-left:8px">{{ detailData.ItemStatus===1?'待处理':'已处理' }}</el-tag>
            </div>
            <h3 style="margin:0 0 10px">{{ detailData.Title }}</h3>
            <el-image v-if="detailData.ImageUrl" :src="detailData.ImageUrl" style="width:100%;max-height:200px;border-radius:6px;margin-bottom:10px" :preview-src-list="[detailData.ImageUrl]"></el-image>
            <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="发布人">{{ detailData.PublisherName }}</el-descriptions-item>
                <el-descriptions-item label="联系方式"><span style="color:#409EFF;font-weight:bold">{{ detailData.ContactInfo }}</span></el-descriptions-item>
                <el-descriptions-item label="地点">{{ detailData.Location }}</el-descriptions-item>
                <el-descriptions-item label="时间">{{ detailData.LostTime }}</el-descriptions-item>
                <el-descriptions-item label="描述" :span="2">{{ detailData.Description }}</el-descriptions-item>
                <el-descriptions-item v-if="detailData.HandleRemark" label="处理备注" :span="2">{{ detailData.HandleRemark }}</el-descriptions-item>
            </el-descriptions>
            <div slot="footer">
                <el-button v-if="Token && detailData.UserId===UserId" type="warning" plain @click="ShowEditModal(detailData.Id);detailShow=false">编 辑</el-button>
                <el-button @click="detailShow=false">关 闭</el-button>
            </div>
        </el-dialog>
        <!-- 发布/编辑弹窗 -->
        <el-dialog :title="formData.Id?'编辑信息':'发布失物招领'" :visible.sync="editShow" width="520px">
            <el-form ref="editFormRef" :model="formData" label-width="110px" size="small" :rules="formRules">
                <el-form-item label="类型" prop="ItemType">
                    <el-radio-group v-model="formData.ItemType">
                        <el-radio :label="1">失物（我丢了东西）</el-radio>
                        <el-radio :label="2">招领（我捡到东西）</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="标题" prop="Title">
                    <el-input v-model.trim="formData.Title" placeholder="如：丢失黑色钱包一个"></el-input>
                </el-form-item>
                <el-form-item label="联系方式" prop="ContactInfo">
                    <el-input v-model.trim="formData.ContactInfo" placeholder="手机号或微信号"></el-input>
                </el-form-item>
                <el-form-item label="丢失/拾取地点" prop="Location">
                    <el-input v-model.trim="formData.Location" placeholder="如：图书馆3楼"></el-input>
                </el-form-item>
                <el-form-item label="时间" prop="LostTime">
                    <el-date-picker v-model="formData.LostTime" type="datetime" placeholder="选择时间" value-format="yyyy-MM-dd HH:mm:ss" style="width:100%"></el-date-picker>
                </el-form-item>
                <el-form-item label="物品描述" prop="Description">
                    <el-input type="textarea" :rows="4" v-model="formData.Description" placeholder="详细描述物品特征，颜色、品牌等"></el-input>
                </el-form-item>
                <el-form-item label="物品图片">
                    <UploadImages v-model="formData.ImageUrl" :limit="1"></UploadImages>
                </el-form-item>
            </el-form>
            <div slot="footer">
                <el-button type="primary" @click="SaveForm">发 布</el-button>
                <el-button @click="editShow=false">取 消</el-button>
            </div>
        </el-dialog>
        <PaginationTable ref="PaginationTableId" url="/LostFound/List" :column="dataColum" :where="where">
            <template v-slot:header>
                <el-button v-if="Token" type="primary" size="mini" plain icon="el-icon-plus" @click="ShowAddModal">新 增</el-button>
            </template>
            <template v-slot:ItemType="scope">
                <el-tag :type="scope.row.ItemType===1?'danger':'success'" size="mini">{{ scope.row.ItemType===1?'失物':'招领' }}</el-tag>
            </template>
            <template v-slot:ItemStatus="scope">
                <el-tag :type="scope.row.ItemStatus===1?'warning':'info'" size="mini">{{ scope.row.ItemStatus===1?'待处理':'已处理' }}</el-tag>
            </template>
            <template v-slot:ImageUrl="scope">
                <el-image v-if="scope.row.ImageUrl" :src="scope.row.ImageUrl" style="width:56px;height:56px;border-radius:4px;object-fit:cover" :preview-src-list="[scope.row.ImageUrl]"></el-image>
                <span v-else style="color:#ccc">无</span>
            </template>
            <template v-slot:Operate="scope">
                <el-button type="primary" size="mini" @click="ShowDetailModal(scope.row)">查看</el-button>
                <el-button v-if="Token && scope.row.UserId===UserId" type="warning" size="mini" @click="ShowEditModal(scope.row.Id)">编辑</el-button>
            </template>
        </PaginationTable>
    </div>
</template>
<script>
import store from '@/store';
import { mapGetters } from 'vuex';
import UploadImages from '@/components/Upload/UploadImages.vue';
import PaginationTable from '@/components/Tables/PaginationTable.vue';

export default {
    name: 'FrontLostFoundList',
    components: { UploadImages, PaginationTable },
    computed: {
        ...mapGetters(['Token','UserInfo','UserId','ColumnType'])
    },
    data() {
        return {
            where: {},
            searchForm: {},
            dataColum: [
                { key: 'Id', hidden: true },
                { key: 'ItemType', title: '类型', width: '90px', type: store.getters.ColumnType.USERDEFINED },
                { key: 'ItemStatus', title: '状态', width: '90px', type: store.getters.ColumnType.USERDEFINED },
                { key: 'Title', title: '标题', width: '180px', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'PublisherName', title: '发布人', width: '110px', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'ContactInfo', title: '联系方式', width: '140px', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'Location', title: '地点', width: '140px', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'LostTime', title: '时间', width: '160px', type: store.getters.ColumnType.SHORTTEXT },
                { key: 'ImageUrl', title: '图片', width: '90px', type: store.getters.ColumnType.USERDEFINED },
                { key: 'Operate', title: '操作', width: '200px', type: store.getters.ColumnType.USERDEFINED },
            ],
            formRules: {
                ItemType: [{ required: true, message: '请选择类型', trigger: 'change' }],
                Title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
                ContactInfo: [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
                Location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
                LostTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
                Description: [{ required: true, message: '请输入物品描述', trigger: 'blur' }],
            },
            formData: { ItemType: 1 },
            editShow: false,
            detailShow: false,
            detailData: {},
        };
    },
    methods: {
        ShowAddModal() {
            if (!this.Token) { this.$message.warning('请先登录'); return; }
            this.formData = { 
                ItemType: 1,
                ItemStatus: 1,
                PublisherName: this.UserInfo.Name || this.UserInfo.UserName,
                CreatorId: this.UserId,
                PublisherId: this.UserId,
                UserId: this.UserId
            };
            this.editShow = true;
        },
        async ShowEditModal(Id) {
            let { Data } = await this.$Post('/LostFound/Get', { Id });
            this.formData = Data;
            this.editShow = true;
        },
        ShowDetailModal(row) {
            this.detailData = row;
            this.detailShow = true;
        },
        async SaveForm() {
            this.$refs.editFormRef.validate(async valid => {
                if (valid) {
                    let { Success } = await this.$Post('/LostFound/CreateOrEdit', this.formData);
                    if (Success) {
                        this.$message.success('发布成功');
                        this.editShow = false;
                        this.$refs.PaginationTableId.Reload(this.searchForm);
                    }
                }
            });
        },
        async SearchClick() {
            this.$refs.PaginationTableId.Reload(this.searchForm);
        },
        async ResetClick() {
            this.searchForm = {};
            this.$refs.PaginationTableId.Reload(this.searchForm);
        },
    },
};
</script>
<style scoped>
.app-container {
    padding: 20px;
}
</style>
