<template>
  <div class="members-page">
    <n-card title="會員管理" :bordered="false">

      <!-- 統計小卡 -->
      <n-space :size="12" style="margin-bottom:16px" wrap>
        <div v-for="s in statCards" :key="s.key" class="stat-chip"
          :style="{ borderColor: s.color }"
          :class="{ active: filters.status === s.key }"
          @click="quickFilter(s.key)"
        >
          <span class="stat-label">{{ s.label }}</span>
          <span class="stat-count" :style="{ color: s.color }">{{ s.count }}</span>
        </div>
      </n-space>

      <!-- 篩選區 -->
      <n-space :size="12" style="margin-bottom:16px" wrap>
        <n-input
          v-model:value="filters.keyword"
          placeholder="搜尋會員姓名、Email 或電話"
          clearable
          style="width: 300px"
          @keydown.enter="handleSearch"
        >
          <template #prefix>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" style="width:16px;height:16px">
              <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </template>
        </n-input>

        <n-select
          v-model:value="filters.status"
          placeholder="會員狀態"
          clearable
          style="width: 140px"
          :options="statusOptions"
        />

        <n-button type="primary" @click="handleSearch">搜尋</n-button>
        <n-button @click="handleReset">重置</n-button>
        <n-button type="success" @click="openCreateModal">＋ 新增會員</n-button>
      </n-space>

      <!-- 表格 -->
      <n-data-table
        :columns="columns"
        :data="members"
        :loading="loading"
        :row-key="(row) => row.memberId"
        :pagination="false"
      />

      <!-- 分頁 -->
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <n-pagination
          v-model:page="currentPage"
          :page-count="totalPages"
          :page-size="pageSize"
          show-quick-jumper
          @update:page="loadMembers"
        />
      </div>
    </n-card>

    <!-- 新增會員 Modal -->
    <n-modal v-model:show="createModalOpen" preset="card" title="新增會員" style="width:460px" :mask-closable="false">
      <n-form ref="createFormRef" :model="createForm" :rules="createRules" label-placement="left" label-width="80">
        <n-form-item label="姓名" path="name">
          <n-input v-model:value="createForm.name" placeholder="請輸入姓名" />
        </n-form-item>
        <n-form-item label="Email" path="email">
          <n-input v-model:value="createForm.email" placeholder="請輸入 Email" />
        </n-form-item>
        <n-form-item label="密碼" path="password">
          <n-input v-model:value="createForm.password" type="password" show-password-on="click" placeholder="至少 6 位" />
        </n-form-item>
        <n-form-item label="身份" path="role">
          <n-select v-model:value="createForm.role" :options="roleSelectOptions" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="createModalOpen = false">取消</n-button>
          <n-button type="primary" :loading="creating" @click="handleCreate">建立</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 會員詳情 Drawer -->
    <n-drawer v-model:show="drawerOpen" :width="480" placement="right">
      <n-drawer-content :title="`會員詳情 — ${drawerMember?.name ?? ''}`" closable>
        <div v-if="detailLoading" style="text-align:center;padding:40px">
          <n-spin size="large" />
        </div>
        <template v-else-if="drawerMember">
          <!-- 頭像 + 基本資訊 -->
          <div style="display:flex;align-items:center;gap:16px;margin-bottom:20px">
            <n-avatar
              round size="64"
              style="background:linear-gradient(135deg,#c5a880 0%,#8b6b4a 100%);color:#fff;font-size:24px;flex-shrink:0"
            >{{ drawerMember.name?.[0] ?? '?' }}</n-avatar>
            <div>
              <div style="font-size:17px;font-weight:700">{{ drawerMember.name }}</div>
              <div style="font-size:13px;color:#888;margin-top:3px">{{ drawerMember.email }}</div>
              <n-space style="margin-top:6px" :size="6">
                <n-tag :type="roleTagType[drawerMember.role]?.type ?? 'default'" size="small" round>
                  {{ roleTagType[drawerMember.role]?.label ?? drawerMember.role }}
                </n-tag>
                <n-tag :type="drawerMember.status === 'ACTIVE' ? 'success' : 'error'" size="small" round>
                  {{ drawerMember.status === 'ACTIVE' ? '啟用中' : '已停用' }}
                </n-tag>
              </n-space>
            </div>
          </div>

          <!-- 權限管理 -->
          <n-card size="small" style="margin-bottom:12px">
            <p style="font-size:12px;color:#888;margin:0 0 8px">權限管理</p>
            <n-space :size="8">
              <n-button
                v-for="r in roleOptions" :key="r.value"
                size="small"
                :type="drawerMember.role === r.value ? r.btnType : 'default'"
                :ghost="drawerMember.role !== r.value"
                :loading="roleUpdating && pendingRole === r.value"
                :disabled="roleUpdating"
                @click="handleUpdateRole(drawerMember, r.value)"
              >{{ r.label }}</n-button>
            </n-space>
          </n-card>

          <!-- 狀態操作 -->
          <n-card size="small" style="margin-bottom:16px">
            <p style="font-size:12px;color:#888;margin:0 0 8px">帳號狀態</p>
            <n-button
              v-if="drawerMember.status === 'ACTIVE'"
              type="error" size="small" ghost
              :loading="statusUpdating"
              @click="handleToggleStatus(drawerMember, 'INACTIVE')"
            >停用帳號</n-button>
            <n-button
              v-else
              type="success" size="small" ghost
              :loading="statusUpdating"
              @click="handleToggleStatus(drawerMember, 'ACTIVE')"
            >啟用帳號</n-button>
          </n-card>

          <n-descriptions :columns="1" label-placement="left" size="small" bordered>
            <n-descriptions-item label="會員 ID">{{ drawerMember.memberId }}</n-descriptions-item>
            <n-descriptions-item label="電話">{{ drawerMember.phone || '—' }}</n-descriptions-item>
            <n-descriptions-item label="訂單數">{{ drawerMember.totalOrders ?? 0 }} 筆</n-descriptions-item>
            <n-descriptions-item label="累計消費">NT$ {{ (drawerMember.totalSpent ?? 0).toLocaleString() }}</n-descriptions-item>
            <n-descriptions-item label="紅利點數">{{ drawerMember.bonusPoints ?? 0 }} 點</n-descriptions-item>
            <n-descriptions-item label="加入時間">{{ formatDate(drawerMember.createdAt) }}</n-descriptions-item>
            <n-descriptions-item label="最後更新">{{ formatDate(drawerMember.updatedAt) }}</n-descriptions-item>
          </n-descriptions>

          <!-- 刪除會員 -->
          <div style="margin-top:24px;padding-top:16px;border-top:1px solid #f0f0f0">
            <n-popconfirm
              @positive-click="handleDelete(drawerMember)"
              positive-text="確認刪除"
              negative-text="取消"
              positive-button-props="{ type: 'error' }"
            >
              <template #trigger>
                <n-button type="error" ghost size="small" :loading="deleting">
                  刪除此會員
                </n-button>
              </template>
              確定要刪除「{{ drawerMember.name }}」嗎？此操作無法復原。
            </n-popconfirm>
          </div>
        </template>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted, computed } from "vue";
import { NButton, NTag, NSpace, NAvatar, NPopconfirm, useMessage, useDialog, type DataTableColumns, type FormInst, type FormRules } from "naive-ui";
import { memberApi, type AdminMember } from "../../../services/member";

const message = useMessage();
const dialog = useDialog();

/* ── 篩選 ── */
const filters = ref({ keyword: "", status: undefined as string | undefined });

const statusOptions = [
  { label: "啟用中", value: "ACTIVE" },
  { label: "已停用", value: "INACTIVE" },
];

const roleTagType: Record<string, { type: string; label: string }> = {
  ADMIN:    { type: "info",    label: "管理員" },
  VIP:      { type: "warning", label: "VIP" },
  CUSTOMER: { type: "default", label: "一般會員" },
};

const roleOptions = [
  { value: "CUSTOMER", label: "一般會員", btnType: "default" as const },
  { value: "VIP",      label: "VIP 會員",  btnType: "warning" as const },
  { value: "ADMIN",    label: "管理員",    btnType: "info"    as const },
];

/* ── 統計小卡 ── */
const statCounts = ref({ ACTIVE: 0, INACTIVE: 0, total: 0 });

const statCards = computed(() => [
  { key: "",         label: "全部會員", color: "#555",    count: statCounts.value.total },
  { key: "ACTIVE",   label: "啟用中",  color: "#18a058", count: statCounts.value.ACTIVE },
  { key: "INACTIVE", label: "已停用",  color: "#d03050", count: statCounts.value.INACTIVE },
]);

const loadStats = async () => {
  try {
    const [all, active, inactive] = await Promise.all([
      memberApi.getMembers({ pageSize: 1 }),
      memberApi.getMembers({ status: "ACTIVE",   pageSize: 1 }),
      memberApi.getMembers({ status: "INACTIVE", pageSize: 1 }),
    ]);
    statCounts.value.total    = all.total;
    statCounts.value.ACTIVE   = active.total;
    statCounts.value.INACTIVE = inactive.total;
  } catch {}
};

const quickFilter = (key: string) => {
  filters.value.status = filters.value.status === key ? undefined : (key || undefined);
  handleSearch();
};

/* ── 資料 ── */
const loading = ref(false);
const members = ref<AdminMember[]>([]);
const currentPage = ref(1);
const pageSize = 20;
const totalPages = ref(1);

const loadMembers = async () => {
  loading.value = true;
  try {
    const res = await memberApi.getMembers({
      keyword: filters.value.keyword || undefined,
      status: filters.value.status || undefined,
      page: currentPage.value,
      pageSize,
    });
    members.value = res.items;
    totalPages.value = res.totalPages;
  } catch (err: any) {
    message.error("載入會員失敗：" + (err.message ?? ""));
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => { currentPage.value = 1; loadMembers(); };
const handleReset = () => { filters.value = { keyword: "", status: undefined }; handleSearch(); };

onMounted(() => { loadMembers(); loadStats(); });

/* ── 表格欄位 ── */
const columns: DataTableColumns<AdminMember> = [
  {
    title: "會員", key: "member", width: 220,
    render: row => h("div", { style: "display:flex;align-items:center;gap:10px" }, [
      h(NAvatar, {
        round: true, size: 36,
        style: "background:linear-gradient(135deg,#c5a880 0%,#8b6b4a 100%);color:#fff;font-size:15px;flex-shrink:0",
      }, () => row.name?.[0] ?? "?"),
      h("div", [
        h("div", { style: "font-weight:600;font-size:14px" }, row.name),
        h("div", { style: "font-size:12px;color:#999" }, row.email),
      ]),
    ]),
  },
  { title: "電話", key: "phone", width: 130, render: row => row.phone || "—" },
  { title: "訂單數", key: "totalOrders", width: 80,
    render: row => h("span", {}, row.totalOrders ?? 0) },
  { title: "消費金額", key: "totalSpent", width: 120,
    render: row => `NT$ ${(row.totalSpent ?? 0).toLocaleString()}` },
  {
    title: "身份", key: "role", width: 100,
    render: row => {
      const cfg = roleTagType[row.role] ?? { type: "default", label: row.role };
      return h(NTag, { type: cfg.type as any, size: "small", round: true }, () => cfg.label);
    },
  },
  {
    title: "狀態", key: "status", width: 90,
    render: row => h(NTag, {
      type: row.status === "ACTIVE" ? "success" : "error",
      size: "small", round: true,
    }, () => row.status === "ACTIVE" ? "啟用" : "停用"),
  },
  { title: "加入日期", key: "createdAt", width: 120,
    render: row => formatDateShort(row.createdAt) },
  {
    title: "操作", key: "actions", width: 140, fixed: "right",
    render: row => h(NSpace, { size: 6 }, () => [
      h(NButton, { size: "small", onClick: () => openDrawer(row.memberId) }, () => "查看"),
      h(NPopconfirm, {
        onPositiveClick: () => handleDeleteFromTable(row),
        positiveText: "確認刪除",
        negativeText: "取消",
      }, {
        trigger: () => h(NButton, { size: "small", type: "error", ghost: true }, () => "刪除"),
        default: () => `確定刪除「${row.name}」？`,
      }),
    ]),
  },
];

/* ── 詳情 Drawer ── */
const drawerOpen = ref(false);
const detailLoading = ref(false);
const drawerMember = ref<AdminMember | null>(null);
const statusUpdating = ref(false);
const roleUpdating = ref(false);
const pendingRole = ref("");
const deleting = ref(false);

const openDrawer = async (memberId: number) => {
  drawerOpen.value = true;
  detailLoading.value = true;
  drawerMember.value = null;
  try {
    drawerMember.value = await memberApi.getMember(memberId);
  } catch {
    message.error("載入會員詳情失敗");
    drawerOpen.value = false;
  } finally {
    detailLoading.value = false;
  }
};

const handleToggleStatus = async (member: AdminMember, newStatus: string) => {
  statusUpdating.value = true;
  try {
    await memberApi.updateStatus(member.memberId, newStatus);
    member.status = newStatus;
    const row = members.value.find(m => m.memberId === member.memberId);
    if (row) row.status = newStatus;
    message.success("狀態更新成功");
    loadStats();
  } catch (err: any) {
    message.error("更新失敗：" + (err.message ?? ""));
  } finally {
    statusUpdating.value = false;
  }
};

const handleUpdateRole = async (member: AdminMember, newRole: string) => {
  if (member.role === newRole) return;
  roleUpdating.value = true;
  pendingRole.value = newRole;
  try {
    await memberApi.updateRole(member.memberId, newRole);
    member.role = newRole;
    const row = members.value.find(m => m.memberId === member.memberId);
    if (row) row.role = newRole;
    message.success("權限更新成功");
  } catch (err: any) {
    message.error("更新失敗：" + (err.message ?? ""));
  } finally {
    roleUpdating.value = false;
    pendingRole.value = "";
  }
};

const handleDelete = async (member: AdminMember) => {
  deleting.value = true;
  try {
    await memberApi.deleteMember(member.memberId);
    members.value = members.value.filter(m => m.memberId !== member.memberId);
    drawerOpen.value = false;
    message.success(`已刪除會員「${member.name}」`);
    loadStats();
  } catch (err: any) {
    message.error("刪除失敗：" + (err.message ?? ""));
  } finally {
    deleting.value = false;
  }
};

const handleDeleteFromTable = async (row: AdminMember) => {
  try {
    await memberApi.deleteMember(row.memberId);
    members.value = members.value.filter(m => m.memberId !== row.memberId);
    message.success(`已刪除會員「${row.name}」`);
    loadStats();
  } catch (err: any) {
    message.error("刪除失敗：" + (err.message ?? ""));
  }
};

/* ── 新增會員 ── */
const createModalOpen = ref(false);
const creating = ref(false);
const createFormRef = ref<FormInst | null>(null);
const createForm = ref({ name: "", email: "", password: "", role: "CUSTOMER" });

const roleSelectOptions = [
  { label: "一般會員", value: "CUSTOMER" },
  { label: "VIP 會員",  value: "VIP" },
  { label: "管理員",    value: "ADMIN" },
];

const createRules: FormRules = {
  name:     [{ required: true, message: "請輸入姓名",  trigger: "blur" }],
  email:    [{ required: true, message: "請輸入 Email", trigger: "blur" },
             { type: "email",  message: "Email 格式不正確", trigger: "blur" }],
  password: [{ required: true, message: "請輸入密碼",  trigger: "blur" },
             { min: 6, message: "密碼至少 6 位", trigger: "blur" }],
};

const openCreateModal = () => {
  createForm.value = { name: "", email: "", password: "", role: "CUSTOMER" };
  createModalOpen.value = true;
};

const handleCreate = async () => {
  try {
    await createFormRef.value?.validate();
  } catch {
    return;
  }
  creating.value = true;
  try {
    await memberApi.createMember(createForm.value);
    message.success("會員已建立");
    createModalOpen.value = false;
    loadMembers();
    loadStats();
  } catch (err: any) {
    message.error("建立失敗：" + (err.message ?? ""));
  } finally {
    creating.value = false;
  }
};

/* ── 格式化 ── */
const formatDate = (d: string) => {
  if (!d) return "—";
  return new Date(d).toLocaleString("zh-TW", {
    year: "numeric", month: "2-digit", day: "2-digit",
    hour: "2-digit", minute: "2-digit",
  });
};
const formatDateShort = (d: string) => {
  if (!d) return "—";
  return new Date(d).toLocaleDateString("zh-TW", {
    year: "numeric", month: "2-digit", day: "2-digit",
  });
};
</script>

<style scoped lang="scss">
.members-page {
  animation: slideInUp var(--transition-base);

  :deep(.n-card) {
    border: none !important;
    box-shadow: var(--shadow) !important;
  }
}

.stat-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 14px;
  border-radius: 20px;
  border: 1.5px solid #ddd;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.15s, border-color 0.15s;
  background: #fff;

  &:hover { background: #f5f5f5; }
  &.active { background: #f0f0f0; }

  .stat-label { color: #555; }
  .stat-count { font-weight: 600; }
}
</style>
