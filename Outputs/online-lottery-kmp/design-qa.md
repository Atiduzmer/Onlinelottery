# 个人中心参考图还原 QA

## Source visual truth

- Source: `D:/ProgramData/Image/9a79bc1b-3524-4fd1-a81c-b38162b65620.png`
- Source pixels: 863 × 1822
- Target state: 深色个人中心、顶部账户信息、余额卡、订单、快捷入口、安全设置、底部导航

## Implementation evidence

- Screenshot: `qa/profile-qa-final-20260728.png`
- Implementation pixels: 1080 × 2400
- Device: Android emulator `emulator-5554`
- Combined comparison: `qa/profile-compare-final-20260728.png` (same final layout; final capture path above includes the last APK install)
- Normalization: 两张图均缩放到 1080px 内容宽度后进行左右对比；实现截图保留系统状态栏与导航栏，用于检查深色系统栏是否跟随主题。

## Findings and fixes

- [P1] 余额卡缺少真实装饰资产。参考图右侧有低对比足球与金色光晕，原实现只有代码渐变。已生成并接入 `profile_balance_gold.png`，保留左侧文字可读负空间。
- [P1] 头像球体质感不足。已生成并接入 `profile_football_avatar.png`，增加蓝色球体、金属边缘和金色反光。
- [P1] 深色系统栏与底部导航未完全跟随主题。已在 Android 宿主同步状态栏、导航栏颜色与明暗图标模式，并修正根容器背景覆盖系统栏区域。
- [P2] 页面纵向节奏比参考图偏松，导致“设置”行被底部导航遮挡。已压缩卡片内边距、快捷入口行距、区块间距；现在四行安全设置可见，整体层级与参考图更接近。
- [P2] 订单状态图标使用统一图标库的不同图标表达，笔触与参考图仍有差异。保留为可访问的 Material vector 图标，避免使用占位符或文字 glyph；后续可继续替换为品牌 SVG 图标集。

## Required fidelity surfaces

- Fonts/typography: 使用项目统一 SansSerif；标题、余额、辅助信息和底部导航层级已按参考图分级。
- Spacing/layout rhythm: 头像区、余额卡、订单卡、4×2 快捷网格、安全列表和底部导航均按同一移动视口重新排列并压缩节奏。
- Colors/tokens: 深黑背景、午夜蓝表面、香槟金重点、蓝色快捷入口、红色数量徽标均映射到主题色；系统栏同步深色状态。
- Image quality/assets: 使用生成的高光足球头像与暗金足球卡片背景，素材已复制到 Compose resources 并在运行截图中验证。
- Copy/content: 账户余额、VIP、成长值、订单状态、快捷入口、安全入口与参考图保持一致。

## Interaction checks

- 点击底部“个人”进入个人中心。
- 点击头像区设置图标或安全列表“设置”进入设置页。
- 充值、提现、订单和快捷入口均保留可点击反馈。
- 余额图标可以切换显示/隐藏余额。
- 深色模式下状态栏、导航栏、底部导航均为深色；亮色/跟随系统逻辑仍由全局主题控制。
- APK 构建：`:androidApp:assembleDebug` 成功。
- Logcat 检查：未发现 `FATAL EXCEPTION` 或 `ANR in`。

## Final result

passed

## Follow-up display fixes (2026-07-29)

- 比赛页比赛时间改为使用主题前景色，深色模式下不再出现深棕色低对比文字。
- 大乐透/排列三/排列五/七星彩选号页的未选号码球改为主题 surfaceVariant，号码在深色模式下清晰可读。
- 设置页主题选项文字与开奖详情页标题、开奖表格背景改为主题色，亮色和深色模式均完成回归截图验证。
- 回归截图：`qa/fixed-matches.png`、`qa/fixed-detail-final.png`、`qa/fixed-result.png`、`qa/fixed-settings-light2.png`、`qa/fixed-settings-follow.png`。

## Hall visual refresh (2026-07-29)

- 移除大厅顶部搜索入口与底部账户余额卡，减少干扰并让彩种入口成为唯一视觉重点。
- 新增奖杯活动横幅，以及足球、篮球、大乐透、排列三、排列五、七星彩六张独立生成的卡片背景；每张素材均保留左侧文字安全区、右侧展示主体。
- 模拟器回归截图：`qa/hall-redesign-20260729.png`。

## Match centre follow-up (2026-07-29)

- 深色模式的比赛日选项改为品牌蓝底白字，避免低对比问题。
- 赛事筛选移除星形/爱心图标；比赛卡的通用星形占位符替换为基于球队名称生成的盾形队徽。
- 足球、篮球页均在模拟器完成回归：`qa/match-crests-20260729.png`、`qa/match-basketball-crests-20260729.png`。
