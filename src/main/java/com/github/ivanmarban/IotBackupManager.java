package com.github.ivanmarban;

import com.github.ivanmarban.backup.BackupService;
import io.micronaut.configuration.picocli.PicocliRunner;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "iot-backup-manager")
@Slf4j
public class IotBackupManager implements Callable<Integer> {

    @Inject
    private BackupService backupService;

    public static void main(String[] args) {
        System.exit(PicocliRunner.execute(IotBackupManager.class, args));
    }

    @Override
    public Integer call() {
        try {
            backupService.createBackup();
        }
        catch (IOException | RuntimeException e) {
            log.error(e.getMessage(), e);
            return CommandLine.ExitCode.SOFTWARE;
        }
        return CommandLine.ExitCode.OK;
    }

}
