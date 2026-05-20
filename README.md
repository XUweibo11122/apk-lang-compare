# APK 多语言字符串资源比较工具

比较两个 APK 中 `<string>` 多语言资源是否一致：语言列表、各语言的 key 集合、以及相同 key 的译文。

## 依赖

- **JRE 17+**
- **[Apktool](https://apktool.org/) 2.x**：仓库自带 [tools/apktool.jar](tools/apktool.jar)，也可使用 `PATH` 中的 apktool 或通过 `--apktool` 指定路径

## 直接下载（免构建）

仓库已包含可执行 fat JAR 与 Apktool：

- 比较工具：[releases/apk-lang-compare-1.0.0-all.jar](releases/apk-lang-compare-1.0.0-all.jar)
- Apktool：[tools/apktool.jar](tools/apktool.jar)
- 克隆仓库后运行：

```powershell
java -jar releases/apk-lang-compare-1.0.0-all.jar app1.apk app2.apk --apktool tools/apktool.jar
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

```bash
java -jar build/libs/apk-lang-compare-1.0.0-all.jar app-v1.apk app-v2.apk
java -jar build/libs/apk-lang-compare-1.0.0-all.jar app-v1.apk app-v2.apk -o report.json
java -jar build/libs/apk-lang-compare-1.0.0-all.jar app-v1.apk app-v2.apk --apktool tools/apktool.jar -q
```

### 选项

| 选项 | 说明 |
|------|------|
| `-o`, `--output <file>` | 输出 JSON 报告 |
| `-q`, `--quiet` | 仅输出摘要 |
| `--apktool <path>` | apktool 路径 |
| `--keep-temp` | 保留 apktool 解码临时目录（调试） |
| `-h`, `--help` | 帮助 |

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
