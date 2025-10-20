# Bug修复说明

## 修复的文件

已修复以下4个文件中的编译错误：

1. `src/main/java/com/gametechbc/traveloptics/overlay/armor_overlay/DarknessOverlay.java`
2. `src/main/java/com/gametechbc/traveloptics/overlay/armor_overlay/PlasmaFuelOverlay.java`
3. `src/main/java/com/gametechbc/traveloptics/overlay/armor_overlay/PrimordialCrestOverlay.java`
4. `src/main/java/com/gametechbc/traveloptics/overlay/armor_overlay/TectonicCrestOverlay.java`

## 修复的问题

这些文件都是反编译后的代码，包含了错误的API调用。主要修复内容：

### 1. 获取装备槽位物品
```java
// 错误（反编译错误）:
ItemStack chestplate = player.shouldRemoveSoulSpeed(EquipmentSlot.CHEST);

// 修复后:
ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
```

### 2. PoseStack操作
```java
// 错误:
PoseStack poseStack = guiGraphics.enableScissor();
poseStack.mulPoseMatrix();

// 修复后:
PoseStack poseStack = guiGraphics.pose();
poseStack.pushPose();
```

### 3. 变换操作
```java
// 错误:
poseStack.mulPoseMatrix(x, y, z);

// 修复后:
poseStack.translate(x, y, z);
```

### 4. 旋转操作
```java
// 错误:
poseStack.mulPoseMatrix(Axis.ZP.rotationDegrees(0.0f));

// 修复后:
poseStack.mulPose(Axis.ZP.rotationDegrees(0.0f));
```

### 5. 缩放操作
```java
// 错误:
poseStack.popPose(1.0f, 1.0f, 1.0f);

// 修复后:
poseStack.scale(1.0f, 1.0f, 1.0f);
```

### 6. 渲染纹理
```java
// 错误:
guiGraphics.renderComponentHoverEffect(TEXTURE, x, y, u, v, width, height, texWidth, texHeight);

// 修复后:
guiGraphics.blit(TEXTURE, x, y, u, v, width, height, texWidth, texHeight);
```

### 7. 物品检查
```java
// 错误:
if (chestplate.onUseTick() || chestplate.setRepairCost() != item) {

// 修复后:
if (chestplate.isEmpty() || chestplate.getItem() != item) {
```

## 如何编译

### 方法1：使用编译脚本
```bash
cd TOtest
./compile_project.sh
```

### 方法2：直接使用Gradle
```bash
cd TOtest
./gradlew compileJava --no-daemon
```

### 完整构建（包括打包）
```bash
cd TOtest
./gradlew build --no-daemon
```

## 注意事项

- 首次编译会下载大量依赖和Minecraft开发环境，可能需要5-10分钟
- 确保网络连接稳定
- 编译完成后，构建的jar文件会在 `build/libs/` 目录下
