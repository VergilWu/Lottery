"""
LSTM 彩票预测模型训练脚本

使用 TensorFlow 训练 LSTM 模型，然后转换为 TensorFlow Lite 格式
用于 Android 应用中的彩票号码预测

依赖:
    pip install tensorflow numpy pandas

使用方法:
    python train_lstm_model.py --lottery_type ssq --history_file history.json
"""

import tensorflow as tf
import numpy as np
import json
import argparse
from pathlib import Path
from typing import List, Tuple

print(f"TensorFlow version: {tf.__version__}")


class LotteryLSTMModel:
    """
    LSTM 彩票预测模型
    
    架构:
        Input: [batch_size, sequence_length, input_features]
        LSTM Layer 1: 128 units
        Dropout: 0.2
        LSTM Layer 2: 64 units
        Dropout: 0.2
        Dense Layer: 32 units (ReLU)
        Output Layer: num_classes (Sigmoid for binary classification of each number)
    """
    
    def __init__(
        self,
        num_red_balls: int = 33,
        num_blue_balls: int = 16,
        sequence_length: int = 10,
        lstm_units_1: int = 128,
        lstm_units_2: int = 64,
        dense_units: int = 32
    ):
        self.num_red_balls = num_red_balls
        self.num_blue_balls = num_blue_balls
        self.sequence_length = sequence_length
        self.lstm_units_1 = lstm_units_1
        self.lstm_units_2 = lstm_units_2
        self.dense_units = dense_units
        
        # 创建两个模型：红球和蓝球
        self.red_model = self._build_model(num_red_balls, "red_lstm")
        self.blue_model = self._build_model(num_blue_balls, "blue_lstm")
    
    def _build_model(self, num_classes: int, name: str) -> tf.keras.Model:
        """构建 LSTM 模型"""
        model = tf.keras.Sequential([
            # 输入层
            tf.keras.layers.Input(shape=(self.sequence_length, num_classes), name=f"{name}_input"),
            
            # LSTM 层 1
            tf.keras.layers.LSTM(
                self.lstm_units_1,
                return_sequences=True,
                name=f"{name}_lstm1"
            ),
            tf.keras.layers.Dropout(0.2, name=f"{name}_dropout1"),
            
            # LSTM 层 2
            tf.keras.layers.LSTM(
                self.lstm_units_2,
                return_sequences=False,
                name=f"{name}_lstm2"
            ),
            tf.keras.layers.Dropout(0.2, name=f"{name}_dropout2"),
            
            # 全连接层
            tf.keras.layers.Dense(
                self.dense_units,
                activation='relu',
                name=f"{name}_dense"
            ),
            
            # 输出层（每个号码的概率）
            tf.keras.layers.Dense(
                num_classes,
                activation='sigmoid',
                name=f"{name}_output"
            )
        ], name=name)
        
        # 编译模型
        model.compile(
            optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
            loss='binary_crossentropy',
            metrics=['accuracy', tf.keras.metrics.Precision(), tf.keras.metrics.Recall()]
        )
        
        return model
    
    def prepare_data(
        self,
        history_data: List[dict]
    ) -> Tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
        """
        准备训练数据
        
        Args:
            history_data: 历史开奖数据列表，格式: [
                {"red": ["01", "05", "12", ...], "blue": ["08", ...]},
                ...
            ]
        
        Returns:
            X_red, y_red, X_blue, y_blue
        """
        # 红球数据
        red_sequences = []
        red_targets = []
        
        # 蓝球数据
        blue_sequences = []
        blue_targets = []
        
        # 滑动窗口创建序列
        for i in range(len(history_data) - self.sequence_length):
            # 红球
            red_seq = []
            for j in range(i, i + self.sequence_length):
                red_vector = np.zeros(self.num_red_balls)
                for num in history_data[j]['red']:
                    red_vector[int(num) - 1] = 1
                red_seq.append(red_vector)
            red_sequences.append(red_seq)
            
            # 红球目标（下一期）
            red_target = np.zeros(self.num_red_balls)
            for num in history_data[i + self.sequence_length]['red']:
                red_target[int(num) - 1] = 1
            red_targets.append(red_target)
            
            # 蓝球
            blue_seq = []
            for j in range(i, i + self.sequence_length):
                blue_vector = np.zeros(self.num_blue_balls)
                for num in history_data[j]['blue']:
                    blue_vector[int(num) - 1] = 1
                blue_seq.append(blue_vector)
            blue_sequences.append(blue_seq)
            
            # 蓝球目标（下一期）
            blue_target = np.zeros(self.num_blue_balls)
            for num in history_data[i + self.sequence_length]['blue']:
                blue_target[int(num) - 1] = 1
            blue_targets.append(blue_target)
        
        return (
            np.array(red_sequences),
            np.array(red_targets),
            np.array(blue_sequences),
            np.array(blue_targets)
        )
    
    def train(
        self,
        history_data: List[dict],
        epochs: int = 100,
        batch_size: int = 32,
        validation_split: float = 0.2
    ):
        """训练模型"""
        print(f"准备训练数据... (历史数据: {len(history_data)} 期)")
        X_red, y_red, X_blue, y_blue = self.prepare_data(history_data)
        
        print(f"红球训练集形状: X={X_red.shape}, y={y_red.shape}")
        print(f"蓝球训练集形状: X={X_blue.shape}, y={y_blue.shape}")
        
        # 训练红球模型
        print("\n" + "="*50)
        print("训练红球 LSTM 模型...")
        print("="*50)
        red_history = self.red_model.fit(
            X_red, y_red,
            epochs=epochs,
            batch_size=batch_size,
            validation_split=validation_split,
            callbacks=[
                tf.keras.callbacks.EarlyStopping(
                    monitor='val_loss',
                    patience=10,
                    restore_best_weights=True
                ),
                tf.keras.callbacks.ReduceLROnPlateau(
                    monitor='val_loss',
                    factor=0.5,
                    patience=5,
                    min_lr=0.00001
                )
            ],
            verbose=1
        )
        
        # 训练蓝球模型
        print("\n" + "="*50)
        print("训练蓝球 LSTM 模型...")
        print("="*50)
        blue_history = self.blue_model.fit(
            X_blue, y_blue,
            epochs=epochs,
            batch_size=batch_size,
            validation_split=validation_split,
            callbacks=[
                tf.keras.callbacks.EarlyStopping(
                    monitor='val_loss',
                    patience=10,
                    restore_best_weights=True
                ),
                tf.keras.callbacks.ReduceLROnPlateau(
                    monitor='val_loss',
                    factor=0.5,
                    patience=5,
                    min_lr=0.00001
                )
            ],
            verbose=1
        )
        
        return red_history, blue_history
    
    def save_models(self, output_dir: str):
        """保存 Keras 模型"""
        output_path = Path(output_dir)
        output_path.mkdir(parents=True, exist_ok=True)
        
        red_path = output_path / "lottery_lstm_red.h5"
        blue_path = output_path / "lottery_lstm_blue.h5"
        
        self.red_model.save(str(red_path))
        self.blue_model.save(str(blue_path))
        
        print(f"\n✅ Keras 模型已保存:")
        print(f"   红球: {red_path}")
        print(f"   蓝球: {blue_path}")
        
        return str(red_path), str(blue_path)
    
    def convert_to_tflite(self, output_dir: str):
        """转换为 TensorFlow Lite 格式"""
        output_path = Path(output_dir)
        output_path.mkdir(parents=True, exist_ok=True)
        
        # 转换红球模型
        print("\n转换红球模型为 TFLite...")
        red_converter = tf.lite.TFLiteConverter.from_keras_model(self.red_model)
        red_converter.optimizations = [tf.lite.Optimize.DEFAULT]
        red_converter.target_spec.supported_types = [tf.float16]  # 使用 float16 减小模型大小
        # 兼容LSTM：启用Select TF Ops并禁用tensor_list_ops降级
        red_converter.target_spec.supported_ops = [
            tf.lite.OpsSet.TFLITE_BUILTINS,
            tf.lite.OpsSet.SELECT_TF_OPS
        ]
        red_converter._experimental_lower_tensor_list_ops = False
        red_tflite = red_converter.convert()
        
        red_tflite_path = output_path / "lottery_lstm_red.tflite"
        with open(red_tflite_path, 'wb') as f:
            f.write(red_tflite)
        
        # 转换蓝球模型
        print("转换蓝球模型为 TFLite...")
        blue_converter = tf.lite.TFLiteConverter.from_keras_model(self.blue_model)
        blue_converter.optimizations = [tf.lite.Optimize.DEFAULT]
        blue_converter.target_spec.supported_types = [tf.float16]
        # 兼容LSTM：启用Select TF Ops并禁用tensor_list_ops降级
        blue_converter.target_spec.supported_ops = [
            tf.lite.OpsSet.TFLITE_BUILTINS,
            tf.lite.OpsSet.SELECT_TF_OPS
        ]
        blue_converter._experimental_lower_tensor_list_ops = False
        blue_tflite = blue_converter.convert()
        
        blue_tflite_path = output_path / "lottery_lstm_blue.tflite"
        with open(blue_tflite_path, 'wb') as f:
            f.write(blue_tflite)
        
        print(f"\n✅ TFLite 模型已保存:")
        print(f"   红球: {red_tflite_path} ({red_tflite_path.stat().st_size / 1024:.2f} KB)")
        print(f"   蓝球: {blue_tflite_path} ({blue_tflite_path.stat().st_size / 1024:.2f} KB)")
        
        return str(red_tflite_path), str(blue_tflite_path)


def load_history_from_json(file_path: str) -> List[dict]:
    """从 JSON 文件加载历史数据"""
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # 假设 JSON 格式为: {"data": [{"red": [...], "blue": [...]}, ...]}
    if isinstance(data, dict) and 'data' in data:
        return data['data']
    elif isinstance(data, list):
        return data
    else:
        raise ValueError("不支持的 JSON 格式")


def generate_sample_data(num_samples: int = 200) -> List[dict]:
    """生成示例数据（用于测试）"""
    print(f"⚠️  使用随机生成的示例数据 ({num_samples} 期)")
    data = []
    for _ in range(num_samples):
        red = sorted(np.random.choice(range(1, 34), size=6, replace=False))
        blue = np.random.choice(range(1, 17), size=1)
        data.append({
            'red': [f"{n:02d}" for n in red],
            'blue': [f"{n:02d}" for n in blue]
        })
    return data


def main():
    parser = argparse.ArgumentParser(description='训练 LSTM 彩票预测模型')
    parser.add_argument('--lottery_type', type=str, default='ssq', help='彩票类型 (ssq, dlt, etc.)')
    parser.add_argument('--history_file', type=str, default=None, help='历史数据 JSON 文件路径')
    parser.add_argument('--output_dir', type=str, default='models', help='输出目录')
    parser.add_argument('--epochs', type=int, default=80, help='训练轮数（100期数据推荐50-100轮）')
    parser.add_argument('--batch_size', type=int, default=16, help='批次大小（100期数据推荐8-16）')
    parser.add_argument('--sequence_length', type=int, default=8, help='序列长度（100期数据推荐5-10）')
    parser.add_argument('--validation_split', type=float, default=0.15, help='验证集比例（默认15%）')
    
    args = parser.parse_args()
    
    print("="*60)
    print("🎲 LSTM 彩票预测模型训练")
    print("="*60)
    print(f"彩票类型: {args.lottery_type}")
    print(f"序列长度: {args.sequence_length}")
    print(f"训练轮数: {args.epochs}")
    print(f"批次大小: {args.batch_size}")
    print(f"验证集比例: {args.validation_split * 100:.0f}%")
    print("="*60)
    
    # 数据量检查和建议
    min_required = args.sequence_length + 10
    print(f"\n📊 数据量检查:")
    print(f"  最小需求: {min_required} 期")
    print(f"  推荐数量: 100+ 期")
    if args.history_file:
        print(f"  数据文件: {args.history_file}")
    
    # 加载历史数据
    if args.history_file and Path(args.history_file).exists():
        print(f"从文件加载历史数据: {args.history_file}")
        history_data = load_history_from_json(args.history_file)
    else:
        history_data = generate_sample_data(200)
    
    # 根据彩票类型设置球号范围
    lottery_config = {
        'ssq': {'red': 33, 'blue': 16},  # 双色球: 红球1-33选6, 蓝球1-16选1
        'dlt': {'red': 35, 'blue': 12},  # 大乐透: 红球1-35选5, 蓝球1-12选2
        'qlc': {'red': 30, 'blue': 30},  # 七乐彩: 基本号1-30选7, 特别号1-30选1
        'fc3d': {'red': 10, 'blue': 10},  # 福彩3D: 百位、十位、个位各0-9
        'qxc': {'red': 10, 'blue': 15},   # 七星彩: 前6位0-9, 第7位0-14
        'pl3': {'red': 10, 'blue': 10},   # 排列3: 百位、十位、个位各0-9
        'pl5': {'red': 10, 'blue': 10},   # 排列5: 5位数字，每位0-9
        'kl8': {'red': 80, 'blue': 1},    # 快乐8: 1-80选20个号码
    }
    
    config = lottery_config.get(args.lottery_type, lottery_config['ssq'])
    
    # 创建模型
    model = LotteryLSTMModel(
        num_red_balls=config['red'],
        num_blue_balls=config['blue'],
        sequence_length=args.sequence_length
    )
    
    # 训练模型
    model.train(
        history_data=history_data,
        epochs=args.epochs,
        batch_size=args.batch_size,
        validation_split=args.validation_split
    )
    
    # 保存模型
    model.save_models(args.output_dir)
    model.convert_to_tflite(args.output_dir)
    
    print("\n" + "="*60)
    print("✅ 训练完成！")
    print("="*60)
    print("\n下一步：")
    print("1. 将 .tflite 文件复制到 Android 项目:")
    print(f"   cp {args.output_dir}/lottery_lstm_*.tflite ../app/src/main/assets/")
    print("2. 在 Android 应用中使用 TensorFlow Lite 加载模型")
    print("3. 运行预测！")


if __name__ == '__main__':
    main()

