<template>
    <div class="app-container">
        <el-card class="box-card">
            <div slot="header" class="clearfix">
                <el-button type="primary" size="mini" icon="el-icon-search" @click="SearchClick">搜 索</el-button>
                <el-button type="warning" size="mini" @click="ResetClick" icon="el-icon-s-promotion">重 置</el-button>
            </div>
            <div class="tb-body">
                <el-form ref="searchFormRef" :model="searchForm" :inline="true" label-width="120px" :style="`flex:1;`">
                    <el-form-item label="流水编号" prop="NoLike">
                        <el-input v-model.trim="searchForm.NoLike" placeholder="请输入流水编号" :clearable="true"></el-input>
                    </el-form-item>
                    <el-form-item label="手机号" prop="PhoneLike">
                        <el-input v-model.trim="searchForm.PhoneLike" placeholder="请输入手机号" :clearable="true"></el-input>
                    </el-form-item>
                    <el-form-item label="姓名" prop="NameLike">
                        <el-input v-model.trim="searchForm.NameLike" placeholder="请输入姓名" :clearable="true"></el-input>
                    </el-form-item>
                    <el-form-item label="自习室">
                        <SigleSelect url="/Room/List" columnName="Name" :clearable="true" columnValue="Id" v-model="searchForm.RoomId">
                        </SigleSelect>
                    </el-form-item>
                    <el-form-item label="座位">
                        <SigleSelect url="/Seat/List" columnName="No" :clearable="true" columnValue="Id" v-model="searchForm.SeatId">
                        </SigleSelect>
                    </el-form-item>
                    <el-form-item label="用户">
                        <SigleSelect url="/User/List" columnName="Name" :clearable="true" columnValue="Id" v-model="searchForm.UserId" :where="{ RoleType: 2 }">
                        </SigleSelect>
                    </el-form-item>
                    <el-form-item label="评分范围">
                        <el-slider v-model="searchForm.CommentScoreRange" range :min="0" :max="5" :step="0.5" show-input style="width: 300px;"></el-slider>
                    </el-form-item>
                </el-form>
            </div>

        </el-card>

        <el-dialog title="评论详情" :visible.sync="detailShow" width="50%" :lock-scroll="true">
            <el-form ref="detailForm" :model="formData" label-width="140px" size="mini">
                <el-row :gutter="10" class="EditFromBody">
                    <el-col :span="24">
                        <el-form-item label="流水编号">
                            <el-input v-model.trim="formData.No" :disabled="true"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="用户信息">
                            <div>{{ formData.UserDto?.Name }} ({{ formData.UserDto?.Phone }})</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="自习室">
                            <div>{{ formData.RoomDto?.Name }}</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="座位">
                            <div>{{ formData.SeatDto?.No }}</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="预约时间">
                            <div>{{ formData.AppointDate }}</div>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="评分">
                            <el-rate v-model="formData.CommentScore" :disabled="true"></el-rate>
                            <span style="margin-left: 10px;">{{ formData.CommentScore }}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="评论内容">
                            <el-input type="textarea" :rows="6" v-model="formData.Comment" :disabled="true"></el-input>
                        </el-form-item>
                    </el-col>
                    
                    <!-- 回复列表 -->
                    <el-col :span="24">
                        <el-form-item label="评论互动">
                            <div v-if="formData.CommentReplyList && formData.CommentReplyList.length > 0">
                                <div v-for="(reply, index) in formData.CommentReplyList" :key="index" 
                                     :class="['reply-item', reply.ReplyUserRole === 2 ? 'user-reply' : 'admin-reply']">
                                    <div class="reply-header">
                                        <span class="reply-author">{{ reply.ReplyUserName }} 
                                            <span v-if="reply.ReplyUserRole === 2" class="user-tag">用户</span>
                                            <span v-else class="admin-tag">管理员</span>
                                        </span>
                                        <span class="reply-time">{{ reply.ReplyTime }}</span>
                                    </div>
                                    <div class="reply-content">{{ reply.ReplyContent }}</div>
                                </div>
                            </div>
                            <div v-else class="no-reply">暂无互动</div>
                        </el-form-item>
                    </el-col>
                    
                    <!-- 回复输入框 -->
                    <el-col :span="24">
                        <el-form-item label="管理员回复">
                            <el-input 
                                type="textarea" 
                                :rows="4" 
                                v-model="formData.ReplyContent" 
                                placeholder="请输入回复内容">
                            </el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>

            <div slot="footer" class="dialog-footer">
                <el-button @click="SaveReply" type="primary" :disabled="!formData.ReplyContent || formData.ReplyContent.trim() === ''">提 交 回 复</el-button>
                <el-button @click="detailShow = false">关 闭</el-button>
            </div>
        </el-dialog>

        <PaginationTable ref="PaginationTableId" url="/AppointRecord/List" :column="dataColum" :where="where">
            <template v-slot:header>
                <!-- 导出评论功能已移除 -->
            </template>
            <template v-slot:Operate="scope">
                <el-button type="primary" size="mini" @click="ShowDetailModal(scope.row.Id)">查 看 详 情</el-button>
            </template>
            <template v-slot:CommentScore="scope">
                <el-rate v-model="scope.row.CommentScore" :disabled="true" :max="5"></el-rate>
                <span style="margin-left: 5px;">{{ scope.row.CommentScore }}</span>
            </template>
            <template v-slot:Comment="scope">
                <div class="comment-content" @click="ShowDetailModal(scope.row.Id)">
                    {{ scope.row.Comment }}
                </div>
            </template>
        </PaginationTable>
    </div>
</template>

<script>
import store from '@/store';
import { mapGetters } from 'vuex'
export default {
    name: "CommentList",
    computed: {
        ...mapGetters([
            'Token', 'UserInfo', 'RoleType', 'HasUserInfo', 'ColumnType', "UserId"
        ])
    },
    data() {
        return {
            where: {
                // 只显示有评论的记录
                AppointStatus: 3 // 预约完成状态，只有完成的预约才有评论
            },
            searchForm: {
                CommentScoreRange: [0, 5],
                CommentScoreMin: 0,
                CommentScoreMax: 5
            },//搜索定义的数据对象
            dataColum: [
                {
                    key: "Id",
                    hidden: true,

                },
                {
                    key: "RoomId",
                    hidden: true,
                },
                {
                    key: "RoomDto.Name",
                    title: "自习室名称",
                    width: "160px",
                    type: store.getters.ColumnType.SHORTTEXT,
                },
                {
                    key: "SeatId",
                    hidden: true,
                },
                {
                    key: "SeatDto.No",
                    title: "座位编号",
                    width: "160px",
                    type: store.getters.ColumnType.SHORTTEXT,
                },
                {
                    key: "UserId",
                    hidden: true,
                },
                {
                    key: "UserDto.Name",
                    title: "用户名称",
                    width: "160px",
                    type: store.getters.ColumnType.SHORTTEXT,
                },
                {
                    key: "No",
                    title: "流水编号",
                    width: "160px",
                    type: store.getters.ColumnType.SHORTTEXT,
                },
                {
                    key: "Phone",
                    title: "手机号",
                    width: "160px",
                    type: store.getters.ColumnType.SHORTTEXT,
                },
                {
                    key: "Name",
                    title: "姓名",
                    width: "160px",
                    type: store.getters.ColumnType.SHORTTEXT,
                },
                {
                    key: "AppointDate",
                    title: "预约日期",
                    width: "160px",
                    type: store.getters.ColumnType.DATE,
                },
                {
                    key: "CommentScore",
                    title: "评分",
                    width: "160px",
                    type: store.getters.ColumnType.USERDEFINED,
                },
                {
                    key: "Comment",
                    title: "评论",
                    width: "200px",
                    type: store.getters.ColumnType.USERDEFINED,
                },
                {
                    title: "操作",
                    width: "160px",
                    key: "Operate",
                    type: store.getters.ColumnType.USERDEFINED,
                },
            ],
            formData: {},//保存或者修改定义的数据对象
            detailShow: false,
        };
    },
    created() {

    },
    methods: {
        //修改属性
        async UpdateEntityAsync(Id, title, data) {
            let { Data } = await this.$PostSigleUpdate(`/AppointRecord/Get`, `/AppointRecord/CreateOrEdit`, Id, title, data);
            this.$refs.PaginationTableId.Reload(this.searchForm);
        },
        /**
         * 点击查看详情
         */
        async ShowDetailModal(Id) {
            let { Data } = await this.$Post(`/AppointRecord/Get`, { Id: Id });
            this.formData = Data;
            this.detailShow = true;
        },
        /**
         * 单个删除的时候会触发
         */
        async ShowDeleteModal(Id) {
            await this.$PostDelete(`/AppointRecord/Delete`, { Id: Id });
            this.$refs.PaginationTableId.Reload(this.searchForm);
        },
        /**
         * 批量删除的时候会触发
         */
        async ShowBatchDeleteModal() {
            var ids = this.$refs.PaginationTableId.GetSelectionRow().map(x => x.Id);
            if (ids.length == 0) {
                this.$message.error("你选择需要删除的记录");
                return;
            }
            await this.$PostDelete(`/AppointRecord/BatchDelete`, { Ids: ids });
            this.$refs.PaginationTableId.Reload(this.searchForm);
        },
        /**
         * 点击表格搜索按钮会触发
         */
        async SearchClick() {
            // 构建评分范围查询条件
            if (this.searchForm.CommentScoreRange && this.searchForm.CommentScoreRange.length === 2) {
                this.searchForm.CommentScoreMin = this.searchForm.CommentScoreRange[0];
                this.searchForm.CommentScoreMax = this.searchForm.CommentScoreRange[1];
            } else {
                delete this.searchForm.CommentScoreMin;
                delete this.searchForm.CommentScoreMax;
            }
            
            this.$refs.PaginationTableId.Reload(this.searchForm);
        },
        /**
         * 点击清空表单会触发
         */
        async ResetClick() {
            this.searchForm = {
                CommentScoreRange: [0, 5]
            };
            // 重置评分范围查询条件
            this.searchForm.CommentScoreMin = 0;
            this.searchForm.CommentScoreMax = 5;
            this.$refs.PaginationTableId.Reload(this.searchForm);
        },
        /**
         * 保存管理员回复
         */
        async SaveReply() {
            if (!this.formData.ReplyContent || this.formData.ReplyContent.trim() === '') {
                this.$message.error('回复内容不能为空');
                return;
            }
            
            try {
                // 构建回复数据
                const replyData = {
                    AppointRecordId: this.formData.Id,
                    ReplyContent: this.formData.ReplyContent.trim(),
                    ReplyUserId: this.UserId // 当前登录的管理员ID
                };
                
                // 发送保存请求
                await this.$Post('/CommentReply/Save', replyData);
                
                this.$message.success('回复保存成功');
                
                // 重新加载评论详情，显示最新回复
                await this.ShowDetailModal(this.formData.Id);
            } catch (error) {
                this.$message.error('回复保存失败：' + error.message);
            }
        },

    },
};
</script>

<style scoped>
.comment-content {
    cursor: pointer;
    color: #666;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.comment-content:hover {
    color: #409EFF;
    text-decoration: underline;
}

.reply-item {
    margin-bottom: 15px;
    padding: 10px;
    border-radius: 4px;
}

.reply-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 5px;
}

.reply-author {
    font-weight: bold;
    color: #333;
}

.reply-time {
    font-size: 12px;
    color: #999;
}

.reply-content {
    color: #666;
    line-height: 1.5;
}

.no-reply {
    color: #999;
    font-style: italic;
    text-align: center;
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
}

/* 管理员回复样式 */
.admin-reply {
    border-left: 3px solid #409EFF;
    background-color: #f5f7fa;
}

/* 用户回复样式 */
.user-reply {
    border-left: 3px solid #67C23A;
    background-color: #f0f9eb;
    margin-left: 20px;
}

/* 用户标签 */
.user-tag {
    display: inline-block;
    margin-left: 5px;
    padding: 2px 6px;
    font-size: 10px;
    background-color: #67C23A;
    color: white;
    border-radius: 10px;
}

/* 管理员标签 */
.admin-tag {
    display: inline-block;
    margin-left: 5px;
    padding: 2px 6px;
    font-size: 10px;
    background-color: #409EFF;
    color: white;
    border-radius: 10px;
}
</style>