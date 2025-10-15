# 彩票工具软件 (Lottery Tool)

一款基于现代化 Android 技术栈开发的彩票工具软件，采用 Jetpack Compose + MVI 架构，提供开奖查询、数据分析、智能预测等功能。

## ✨ 功能特性

### 🏠 首页 - 开奖查询
- **彩种选择器**: 支持双色球、大乐透、福彩3D、七乐彩、七星彩、排列3/5、快乐8等
- **开奖信息展示**: 大号码球展示，支持不同彩种布局
- **号码验证工具**: 手动输入号码，自动校验中奖情况
- **开奖历史**: 快速查看历史开奖记录

### 📊 数据分析
- **遗漏分析**: 号码遗漏期数、最大遗漏值、平均遗漏值
- **频率分析**: 出现次数统计、频率分布图
- **冷热号分析**: 热号、温号、冷号动态评分
- **连号分析**: 连续号码组合统计
- **和值分析**: 开奖号码和值统计和走势
- **跨度分析**: 最大号与最小号差值分析
- **AC值分析**: 号码复杂度分析
- **奇偶比/大小比/质合比**: 多维度比例分析
- **区间分析**: 号码区间分布统计

### 🤖 智能预测
- **频率权重法**: 基于历史频率计算权重
- **遗漏补偿法**: 长期未出现号码补偿权重
- **趋势预测法**: 移动平均法预测趋势
- **关联规则挖掘**: Apriori算法挖掘号码组合
- **马尔可夫链预测**: 号码转移概率矩阵
- **贝叶斯概率模型**: 先验概率和条件概率结合
- **LSTM神经网络**: TensorFlow Lite深度学习预测

### 👤 个人中心
- **主题切换**: 日间/夜间/跟随系统
- **语言设置**: 中文/英文切换
- **缓存管理**: 智能缓存清理
- **关于应用**: 应用信息和开源地址

## 🛠 技术栈

### 核心架构
- **UI框架**: Jetpack Compose + Material 3
- **架构模式**: MVI (Model-View-Intent)
- **异步处理**: Kotlin Coroutines + Flow
- **依赖注入**: 手动依赖注入 (轻量级)
- **本地存储**: Room + DataStore

### 机器学习
- **深度学习**: TensorFlow Lite
- **预测模型**: LSTM 神经网络
- **数据处理**: Kotlin 协程并行处理

### UI设计
- **设计风格**: Liquid Glass (毛玻璃效果)
- **动画系统**: Compose Animation
- **图表库**: MPAndroidChart
- **图片加载**: Coil

### 网络与数据
- **网络请求**: Ktor Client
- **数据序列化**: Kotlinx Serialization
- **缓存策略**: LRU Cache + 智能过期
- **API接口**: 第三方彩票数据API

## 📱 界面预览

### 主界面
- 底部导航栏设计，支持四个主要功能模块
- Liquid Glass 毛玻璃效果，现代化UI设计
- 支持日间/夜间主题切换

### 开奖查询
- 大号码球展示，直观显示开奖结果
- 号码验证面板，支持手动输入校验
- 彩种选择器，横向滚动卡片设计

### 数据分析
- 多维度分析图表
- 遗漏走势图、频率柱状图
- 热力图、趋势折线图

### 智能预测
- 推荐号码卡片展示
- 算法解释面板
- 概率分布图

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 17
- Android SDK 24+ (Android 7.0+)
- Kotlin 1.9.0+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/VergilWu/Lottery.git
   cd Lottery
   ```

2. **配置API密钥**
   
   复制 `local.properties.template` 为 `local.properties`：
   ```bash
   cp local.properties.template local.properties
   ```
   
   编辑 `local.properties`，填入您的API密钥：
   ```properties
   LOTTERY_API_KEY=your_api_key_here
   ```

3. **构建项目**
   ```bash
   ./gradlew assembleDebug
   ```

4. **运行应用**
   ```bash
   ./gradlew installDebug
   ```

### API密钥获取

1. 访问 [彩票API服务](https://www.szxk365.com)
2. 注册账号并获取API密钥
3. 将密钥配置到 `local.properties` 文件中

## 📁 项目结构

```
app/src/main/java/com/vergil/lottery/
├── core/                    # 核心模块
│   ├── constants/           # 常量定义
│   ├── background/          # 背景管理
│   ├── ui/                  # UI工具
│   ├── util/                # 工具类
│   └── mvi/                 # MVI架构基类
├── data/                    # 数据层
│   ├── local/               # 本地数据
│   ├── remote/              # 远程数据
│   ├── cache/               # 缓存管理
│   └── repository/          # 数据仓库
├── domain/                  # 业务逻辑层
│   ├── model/               # 数据模型
│   ├── analyzer/            # 分析引擎
│   └── ml/                   # 机器学习
├── presentation/            # 表现层
│   ├── screens/             # 屏幕组件
│   ├── components/           # 可复用组件
│   ├── navigation/          # 导航
│   └── theme/                # 主题系统
└── di/                      # 依赖注入
```

## 🔧 开发指南

### 代码规范
- 遵循 Kotlin 官方编码规范
- 使用 MVI 架构模式
- 所有用户可见文本必须国际化
- 使用 Compose 最佳实践

### 性能优化
- 使用 `LazyColumn` 等懒加载组件
- 合理使用 `remember` 避免重组
- 协程并行处理计算密集型任务
- LRU缓存优化数据访问

### 测试策略
- 单元测试覆盖 ViewModel 和 UseCase
- UI测试覆盖关键用户流程
- 使用 MockK 进行 Mock 测试

## 📊 算法说明

### 数据分析算法
- **遗漏计算**: O(n) 时间复杂度，滑动窗口算法
- **频率统计**: 使用 TreeMap 维护有序统计
- **连号检测**: 动态规划算法优化
- **AC值计算**: 位运算优化奇偶判断

### 预测算法
- **综合评分**: 多算法加权融合
- **并行计算**: 协程 async/await 并行执行
- **缓存优化**: 预测结果缓存，避免重复计算

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 开源协议

本项目采用 MIT 协议 - 查看 [LICENSE](LICENSE) 文件了解详情。

## ⚠️ 免责声明

**本应用仅供学习和研究使用，不构成任何投注建议。**

### 重要声明
- **非投注服务**: 本应用不提供任何形式的投注服务，不参与任何赌博活动
- **数据来源**: 所有数据来源于公开渠道，预测算法仅用于技术研究
- **风险承担**: 用户使用本应用即表示同意承担所有风险，开发者不承担任何法律责任
- **预测结果**: 预测结果仅供参考，不保证准确性，请理性对待
- **合规使用**: 请遵守当地法律法规，合理使用本应用
- **责任限制**: 开发者不承担因使用本应用而产生的任何直接或间接损失

### 详细法律声明
请查看 [DISCLAIMER.md](DISCLAIMER.md) 了解完整的法律免责声明和使用条款。

### 使用条款
1. 本应用仅供学习、研究和技术交流使用
2. 禁止将本应用用于任何商业投注活动
3. 用户应遵守当地法律法规，不得用于非法用途
4. 开发者保留随时修改或终止服务的权利
5. 使用本应用即表示同意上述条款

## 📞 联系我们

- **开发者**: Vergil
- **邮箱**: vergilcat@gmail.com
- **GitHub**: [https://github.com/VergilWu/Lottery](https://github.com/VergilWu/Lottery)

## 🙏 致谢

感谢以下开源项目的支持：
- [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)
- [TensorFlow Lite](https://www.tensorflow.org/lite)
- [Ktor](https://ktor.io/)
- [Room](https://developer.android.com/training/data-storage/room)
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)

---

**⭐ 如果这个项目对您有帮助，请给我们一个 Star！**