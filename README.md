# NoteMaster

一款功能完整的 Android 笔记管理应用。

## 功能特性

- **笔记管理** — 创建、编辑、删除笔记，支持分类筛选和实时搜索
- **笔记置顶** — 长按笔记可置顶/取消置顶，置顶笔记自动排列在顶部
- **笔记锁定** — 为笔记设置密码保护，打开时需输入密码验证
- **笔记提醒** — 设置日期时间提醒，到时发送本地通知
- **拖拽排序** — 长按拖拽调整笔记顺序
- **标签系统** — 创建和管理标签，为笔记添加多个标签
- **分类管理** — 创建、编辑、删除分类，按分类筛选笔记
- **回收站** — 删除的笔记可恢复或永久删除
- **深色模式** — 设置中可切换深色/浅色主题

## 技术架构

- **语言**: Java
- **架构**: 单 Activity + Navigation Component + ViewModel + LiveData
- **数据库**: 原生 SQLite（非 Room）
- **最低 SDK**: 24 (Android 7.0)
- **目标 SDK**: 36

## 项目结构

```
app/src/main/java/com/example/notemaster/
├── model/          # 数据模型 (Note, Category, Tag)
├── data/           # 数据访问层 (DAO + Repository)
├── viewmodel/      # ViewModel 层
├── ui/
│   ├── note/       # 笔记相关界面 (列表、编辑、回收站)
│   ├── category/   # 分类管理界面
│   ├── search/     # 搜索界面
│   └── settings/   # 设置界面
└── util/           # 工具类 (DatabaseHelper, ReminderHelper)
```

## 构建运行

```bash
cd NoteMaster
./gradlew assembleDebug    # 构建调试 APK
./gradlew test             # 运行单元测试
./gradlew lint             # 代码检查
```

## 依赖

- Navigation Component 2.7.7
- Lifecycle (ViewModel + LiveData) 2.7.0
- Glide 4.16.0
- Material Design 1.10.0
- RecyclerView 1.3.2
