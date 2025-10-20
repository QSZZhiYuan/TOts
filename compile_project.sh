#!/bin/bash

echo "开始编译项目..."
echo "这可能需要几分钟时间来下载依赖和设置环境..."
echo ""

cd "$(dirname "$0")"
./gradlew compileJava --no-daemon --stacktrace

if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo "编译成功！"
    echo "======================================"
else
    echo ""
    echo "======================================"
    echo "编译失败，请查看上面的错误信息"
    echo "======================================"
    exit 1
fi
