package com.apkcompare.lang.extractor;

import com.apkcompare.lang.model.ExtractionResult;
import java.nio.file.Path;

public interface StringResourceExtractor {

    ExtractionResult extract(Path apkPath) throws Exception;
}
