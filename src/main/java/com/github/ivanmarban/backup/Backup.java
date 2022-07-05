package com.github.ivanmarban.backup;

import java.nio.file.Path;

public interface Backup {

    void create(Path output);

}
