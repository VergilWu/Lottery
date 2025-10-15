"""
彩票历史数据获取脚本

从彩票API获取历史开奖数据并保存为JSON格式，供LSTM模型训练使用

依赖:
    pip install requests

使用方法:
    python fetch_history_data.py --lottery_type ssq --count 100 --output history_ssq.json
"""

import requests
import json
import time
import argparse
import os
from pathlib import Path
from typing import List, Dict, Optional

# API配置（使用项目的API）
API_BASE_URL = "https://www.szxk365.com/api/openapi.lottery/"
API_KEY = os.getenv("LOTTERY_API_KEY", "YOUR_API_KEY_HERE")  # 从环境变量读取API密钥

# 彩票类型映射
LOTTERY_TYPES = {
    'ssq': {
        'code': 'ssq',
        'name': '双色球',
        'red_count': 6,
        'blue_count': 1
    },
    'dlt': {
        'code': 'cjdlt',
        'name': '大乐透',
        'red_count': 5,
        'blue_count': 2
    },
    'qlc': {
        'code': 'qlc',
        'name': '七乐彩',
        'red_count': 7,
        'blue_count': 1
    },
    'fc3d': {
        'code': 'fc3d',
        'name': '福彩3D',
        'red_count': 3,  # 百位、十位、个位
        'blue_count': 0  # 没有蓝球
    },
    'qxc': {
        'code': '7xc',
        'name': '七星彩',
        'red_count': 6,  # 前6位数字
        'blue_count': 1  # 第7位数字
    },
    'pl3': {
        'code': 'pl3',
        'name': '排列3',
        'red_count': 3,  # 百位、十位、个位
        'blue_count': 0  # 没有蓝球
    },
    'pl5': {
        'code': 'pl5',
        'name': '排列5',
        'red_count': 5,  # 5位数字
        'blue_count': 0  # 没有蓝球
    },
    'kl8': {
        'code': 'kl8',
        'name': '快乐8',
        'red_count': 20,  # 从1-80中选20个
        'blue_count': 0   # 没有蓝球
    }
}


class LotteryDataFetcher:
    """彩票数据获取器"""
    
    def __init__(self, api_key: str = API_KEY):
        self.api_key = api_key
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })
    
    def fetch_history(
        self,
        lottery_code: str,
        count: int = 100,
        retry: int = 3
    ) -> Optional[List[Dict]]:
        """
        获取历史开奖数据
        
        Args:
            lottery_code: 彩票代码 (ssq, cjdlt, etc.)
            count: 获取期数
            retry: 重试次数
        
        Returns:
            历史数据列表或None
        """
        url = f"{API_BASE_URL}history"
        params = {
            'apikey': self.api_key,
            'code': lottery_code,
            'size': count
        }
        
        for attempt in range(retry):
            try:
                print(f"  尝试 {attempt + 1}/{retry}: 请求 {count} 期数据...")
                response = self.session.get(url, params=params, timeout=30)
                response.raise_for_status()
                
                data = response.json()
                
                # 检查多种可能的成功响应格式
                if 'data' in data and isinstance(data['data'], list) and len(data['data']) > 0:
                    print(f"  ✅ 成功获取 {len(data['data'])} 期数据")
                    return data['data']
                elif data.get('code') == 0 and 'data' in data:
                    print(f"  ✅ 成功获取 {len(data['data'])} 期数据")
                    return data['data']
                else:
                    print(f"  ⚠️  API返回错误: code={data.get('code')}, msg={data.get('msg', 'Unknown error')}")
                    print(f"  响应数据: {str(data)[:200]}...")
                    
            except requests.RequestException as e:
                print(f"  ❌ 请求失败: {e}")
                if attempt < retry - 1:
                    wait_time = 2 ** attempt  # 指数退避
                    print(f"  等待 {wait_time} 秒后重试...")
                    time.sleep(wait_time)
        
        return None
    
    def convert_to_training_format(
        self,
        raw_data: List[Dict],
        lottery_type: str
    ) -> List[Dict]:
        """
        转换为训练格式
        
        Args:
            raw_data: API返回的原始数据
            lottery_type: 彩票类型
        
        Returns:
            训练格式的数据列表
        """
        training_data = []
        config = LOTTERY_TYPES.get(lottery_type, LOTTERY_TYPES['ssq'])
        
        for item in raw_data:
            try:
                # 解析红球和蓝球
                red = item.get('red', '').strip().split()
                blue = item.get('blue', '').strip().split() if item.get('blue', '').strip() else []
                
                # 确保格式正确
                red_valid = len(red) >= config['red_count']
                blue_valid = config['blue_count'] == 0 or len(blue) >= config['blue_count']
                
                if red_valid and blue_valid:
                    training_data.append({
                        'red': red[:config['red_count']],
                        'blue': blue[:config['blue_count']] if config['blue_count'] > 0 else [],
                        'issue': item.get('issue', ''),
                        'date': item.get('drawdate', '')
                    })
            except Exception as e:
                print(f"  ⚠️  解析数据失败: {item.get('issue', 'Unknown')}, {e}")
                continue
        
        return training_data


def save_to_json(data: List[Dict], output_file: str):
    """保存为JSON文件"""
    output_path = Path(output_file)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump({
            'data': data,
            'count': len(data),
            'created_at': time.strftime('%Y-%m-%d %H:%M:%S')
        }, f, ensure_ascii=False, indent=2)
    
    print(f"\n✅ 数据已保存到: {output_path}")
    print(f"   总期数: {len(data)}")
    print(f"   文件大小: {output_path.stat().st_size / 1024:.2f} KB")


def load_from_json(input_file: str) -> Optional[List[Dict]]:
    """从JSON文件加载数据"""
    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
            if isinstance(data, dict) and 'data' in data:
                return data['data']
            elif isinstance(data, list):
                return data
        return None
    except Exception as e:
        print(f"❌ 加载文件失败: {e}")
        return None


def validate_data(data: List[Dict], lottery_type: str) -> bool:
    """验证数据格式"""
    if not data or len(data) == 0:
        print("❌ 数据为空")
        return False
    
    config = LOTTERY_TYPES.get(lottery_type, LOTTERY_TYPES['ssq'])
    
    # 检查前几条数据
    for i, item in enumerate(data[:5]):
        if 'red' not in item or 'blue' not in item:
            print(f"❌ 第 {i+1} 条数据缺少必要字段")
            return False
        
        if len(item['red']) != config['red_count']:
            print(f"❌ 第 {i+1} 条数据红球数量不正确: {len(item['red'])} (期望 {config['red_count']})")
            return False
        
        if len(item['blue']) != config['blue_count']:
            print(f"❌ 第 {i+1} 条数据蓝球数量不正确: {len(item['blue'])} (期望 {config['blue_count']})")
            return False
    
    print(f"✅ 数据格式验证通过")
    return True


def main():
    parser = argparse.ArgumentParser(description='获取彩票历史数据')
    parser.add_argument('--lottery_type', type=str, default='ssq', 
                       choices=['ssq', 'dlt', 'qlc', 'fc3d', 'qxc', 'pl3', 'pl5', 'kl8'],
                       help='彩票类型')
    parser.add_argument('--count', type=int, default=100, 
                       help='获取期数（推荐100-200期）')
    parser.add_argument('--output', type=str, default='history.json',
                       help='输出文件路径')
    parser.add_argument('--api_key', type=str, default=API_KEY,
                       help='API Key')
    parser.add_argument('--validate_only', action='store_true',
                       help='仅验证现有文件，不获取新数据')
    
    args = parser.parse_args()
    
    print("="*60)
    print("📥 彩票历史数据获取")
    print("="*60)
    
    if args.validate_only:
        # 验证模式
        print(f"验证文件: {args.output}")
        data = load_from_json(args.output)
        if data:
            validate_data(data, args.lottery_type)
        return
    
    # 获取模式
    config = LOTTERY_TYPES[args.lottery_type]
    print(f"彩票类型: {config['name']} ({config['code']})")
    print(f"获取期数: {args.count}")
    print(f"输出文件: {args.output}")
    print("="*60)
    
    # 创建获取器
    fetcher = LotteryDataFetcher(args.api_key)
    
    # 获取数据
    print("\n📡 开始获取数据...")
    raw_data = fetcher.fetch_history(config['code'], args.count)
    
    if not raw_data:
        print("\n❌ 获取数据失败")
        print("\n💡 提示:")
        print("1. 检查 API Key 是否正确")
        print("2. 检查网络连接")
        print("3. 查看 API 文档: docs/backend/lottery_api_integration.md")
        return
    
    # 转换格式
    print("\n🔄 转换数据格式...")
    try:
        training_data = fetcher.convert_to_training_format(raw_data, args.lottery_type)
        
        if not training_data:
            print("❌ 数据转换失败：没有有效数据")
            print(f"原始数据示例：{raw_data[0] if raw_data else 'None'}")
            return
    except Exception as e:
        print(f"❌ 数据转换失败：{e}")
        import traceback
        traceback.print_exc()
        return
    
    # 验证数据
    print("\n🔍 验证数据格式...")
    if not validate_data(training_data, args.lottery_type):
        return
    
    # 保存数据
    print("\n💾 保存数据...")
    save_to_json(training_data, args.output)
    
    # 显示示例
    print("\n📋 数据示例 (最近3期):")
    for i, item in enumerate(training_data[:3]):
        print(f"  期号 {item.get('issue', 'N/A')}: "
              f"红球 {' '.join(item['red'])}  蓝球 {' '.join(item['blue'])}")
    
    print("\n" + "="*60)
    print("✅ 数据获取完成！")
    print("="*60)
    print("\n下一步：")
    print(f"  python train_lstm_model.py --history_file {args.output} --lottery_type {args.lottery_type}")


if __name__ == '__main__':
    main()

