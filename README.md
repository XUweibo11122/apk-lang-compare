# APK 多语言字符串资源比较工具

比较两个 APK 中 `<string>` 多语言资源是否一致：语言列表、各语言的 key 集合、以及相同 key 的译文。

支持 **主包 `res/values*`**（apktool 解码）与 **`langs/*.br` 语言包**（Brotli → ZIP → 各语言 `.lpk`）一并比较。

## 依赖

- **JRE 17+**
- **[Apktool](https://apktool.org/) 2.x**：仓库自带 [tools/apktool.jar](tools/apktool.jar)，也可使用 `PATH` 中的 apktool 或通过 `--apktool` 指定路径

## 直接下载（免构建）

仓库已包含可执行 fat JAR 与 Apktool：

- 比较工具：[releases/apk-lang-compare-1.0.0-all.jar](releases/apk-lang-compare-1.0.0-all.jar)（与 `tools/apk-lang-compare-1.0.0-all.jar` 相同）
- Apktool：[tools/apktool.jar](tools/apktool.jar)
- 克隆仓库后运行：

```powershell
java -jar tools/apk-lang-compare-1.0.0-all.jar app1.apk app2.apk --apktool tools/apktool.jar
```

## 构建

```bash
cd apk-lang-compare
gradle wrapper
./gradlew build
```

Windows:

```powershell
cd apk-lang-compare
gradle wrapper
.\gradlew.bat build
```

产物：`build/libs/apk-lang-compare-1.0.0-all.jar`

## 用法

### 比较两个 APK（子命令 `compare`，可省略）

```bash
java -jar tools/apk-lang-compare-1.0.0-all.jar compare app-v1.apk app-v2.apk
java -jar tools/apk-lang-compare-1.0.0-all.jar app-v1.apk app-v2.apk
java -jar tools/apk-lang-compare-1.0.0-all.jar compare app-v1.apk app-v2.apk -o report.json
java -jar tools/apk-lang-compare-1.0.0-all.jar compare app-v1.apk app-v2.apk --apktool tools/apktool.jar -q
```

### 导出单个 APK 的字符串 Map（子命令 `dump`）

解码结果**保留在磁盘**，同时将 `Map<语言标签, Map<key, value>>` 写入 JSON：

```powershell
java -jar tools/apk-lang-compare-1.0.0-all.jar dump app.apk -o strings-map.json --apktool tools/apktool.jar
java -jar tools/apk-lang-compare-1.0.0-all.jar dump app.apk -o strings-map.json -d D:\out\app-decoded
```

- `-o` / `--output`：JSON 输出路径（必填）
- `-d` / `--decode-dir`：apktool 解码目录（可选，默认 APK 同目录下 `<apk名>-decoded`）

JSON 示例：

```json
{
  "apk": "D:/app.apk",
  "decodeDir": "D:/app-decoded",
  "stringCount": 1000,
  "locales": {
    "default": { "app_name": "My App" },
    "zh-CN": { "app_name": "我的应用" }
  },
  "warnings": []
}
```

解码后的 `res/values*`、`AndroidManifest.xml` 等在 `decodeDir` 中，可自行查看。

### 选项

| 选项 | 说明 |
|------|------|
| `-o`, `--output <file>` | 输出 JSON 报告 |
| `-q`, `--quiet` | 仅输出摘要 |
| `--apktool <path>` | apktool 路径 |
| `--keep-temp` | 保留 apktool 解码临时目录（调试） |
| `--no-langs` | 不解析 APK 内 `langs/*.br` 语言包 |
| `-h`, `--help` | 帮助 |

### `langs/*.br` 语言包（LPK）

部分 APK 将**除默认语言外**的资源打成 `.lpk`，再打包为 Brotli 压缩的 `.br`，放在 APK 内 `langs/` 目录，例如 `langs/pack.br`。

比较时会自动：

1. 从 APK（ZIP）读取 `langs/**/*.br`
2. Brotli 解压 → 一般为 ZIP，内含 `en.lpk`、`zh-rCN.lpk` 等
3. 每个 `.lpk` 按 ZIP 解析其中的 `res/values*/**/*.xml` 的 `<string>`（若非 ZIP 则回退 apktool 解码）
4. 与主包 apktool 解码结果合并后，再与另一个 APK 做完整 diff

`.lpk` 需为由资源 APK 导出的 ZIP 结构（含 `res/values-*`）；文件名用于推断语言，如 `zh-rCN.lpk` → `zh-CN`。

### 退出码

| 码 | 含义 |
|----|------|
| 0 | 完全一致（无语言/key/译文差异，且无提取警告） |
| 1 | 存在差异 |
| 2 | 参数错误、apktool 未找到、解码或解析失败 |

## 比较范围

- 扫描 `res/values` 与 `res/values-*` 下所有 `.xml` 中的 `<string name="...">`
- `values` → 语言标签 `default`；`values-zh-rCN` → `zh-CN`
- 精确字符串比较（含 `%s`、`%1$d` 等占位符）
- 不包含：`<plurals>`、`<string-array>`

## 限制

- 需能成功用 apktool 解码的 APK（内部使用 `apktool d -f -s`：只解码资源，不解码 smali）
- `values-night`、`values-v21` 等非语言配置目录若含 string 也会参与比较
- 同语言同 key 在多个 XML 中出现时，后者覆盖前者并产生 warning

## Roadmap

- 支持 `<plurals>` / `<string-array>`
- 可选忽略 key 白名单
- 占位符语义等价比较
- 使用 `aapt2 dump` 作为备选提取后端
