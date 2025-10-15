#!/bin/bash

# LSTM 模型训练快速启动脚本
# 适用于 100 期历史数据

echo "======================================================"
echo "🤖 LSTM 模型训练 - 快速启动"
echo "======================================================"

# 检查Python环境
echo ""
echo "📋 检查环境..."
if ! command -v python3 &> /dev/null; then
    echo "❌ Python3 未安装"
    exit 1
fi
echo "✅ Python3: $(python3 --version)"

# 检查依赖
echo ""
echo "📦 检查依赖..."
python3 -c "import tensorflow; import numpy; import pandas" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "⚠️  依赖未安装，正在安装..."
    pip3 install -r requirements.txt
else
    echo "✅ 依赖已安装"
fi

# 创建必要目录
mkdir -p data models

# 提示用户准备数据
echo ""
echo "======================================================"
echo "📊 数据准备"
echo "======================================================"
echo ""
echo "方式1: 使用自动获取脚本（推荐）"
echo "  python3 fetch_history_data.py \\"
echo "    --lottery_type ssq \\"
echo "    --count 100 \\"
echo "    --output data/history_ssq_100.json \\"
echo "    --api_key YOUR_API_KEY"
echo ""
echo "方式2: 手动准备数据"
echo "  将历史数据放在 data/history.json"
echo ""

# 检查数据文件
DATA_FILE=""
if [ -f "data/history_ssq_100.json" ]; then
    DATA_FILE="data/history_ssq_100.json"
    echo "✅ 发现数据文件: $DATA_FILE"
elif [ -f "data/history.json" ]; then
    DATA_FILE="data/history.json"
    echo "✅ 发现数据文件: $DATA_FILE"
else
    echo "⚠️  未找到数据文件"
    echo ""
    read -p "是否使用随机数据进行测试训练？(y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ 已取消"
        exit 1
    fi
    echo "⚠️  将使用随机数据（仅用于测试）"
fi

# 开始训练
echo ""
echo "======================================================"
echo "🏋️  开始训练"
echo "======================================================"
echo ""

if [ -z "$DATA_FILE" ]; then
    # 使用随机数据测试
    echo "⚡ 配置: 测试模式（随机数据）"
    python3 train_lstm_model.py \
        --epochs 30 \
        --batch_size 16 \
        --sequence_length 8 \
        --output_dir models/test
else
    # 使用真实数据训练
    echo "⚡ 配置: 标准模式（100期数据推荐配置）"
    echo "  数据文件: $DATA_FILE"
    echo "  训练轮数: 80"
    echo "  批次大小: 16"
    echo "  序列长度: 8"
    echo ""
    
    python3 train_lstm_model.py \
        --history_file "$DATA_FILE" \
        --lottery_type ssq \
        --epochs 80 \
        --batch_size 16 \
        --sequence_length 8 \
        --validation_split 0.15 \
        --output_dir models/v1_100epochs
fi

# 检查训练结果
echo ""
echo "======================================================"
echo "📦 训练完成检查"
echo "======================================================"

if [ -f "models/v1_100epochs/lottery_lstm_red.tflite" ]; then
    echo "✅ 模型文件已生成"
    echo ""
    ls -lh models/v1_100epochs/*.tflite
    
    echo ""
    echo "======================================================"
    echo "📱 下一步：部署到 Android"
    echo "======================================================"
    echo ""
    echo "1. 复制模型到 Android 项目:"
    echo "   mkdir -p ../app/src/main/assets/"
    echo "   cp models/v1_100epochs/*.tflite ../app/src/main/assets/"
    echo ""
    echo "2. 构建 Android 应用:"
    echo "   cd .."
    echo "   ./gradlew assembleDebug"
    echo ""
    echo "3. 查看详细文档:"
    echo "   docs/LSTM_TRAINING_GUIDE.md"
    echo ""
else
    echo "❌ 训练失败，未生成模型文件"
    echo "请检查错误信息"
fi

echo ""
echo "🎉 完成！"

