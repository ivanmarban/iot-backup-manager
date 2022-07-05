package com.github.ivanmarban.compress;

import java.nio.file.Path;
import java.util.List;

public interface Compressor {

    void compressFiles(List<Path> files, Path output);

    void compressFolder(Path folder, Path output);

}
