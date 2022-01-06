package ua.lysenko.andrii.zip.web;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ua.lysenko.andrii.zip.Zipper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public abstract class FileSystemService implements StorageService {

    protected final Path baseDir;
    protected final Path tmpZipDir;

    public FileSystemService(Path baseDir, Path tmpZipDir) {
        this.baseDir = baseDir;
        this.tmpZipDir = baseDir.getParent().resolve(tmpZipDir);
    }

    public void init() throws IOException {
        Files.createDirectories(baseDir);
        Files.createDirectories(tmpZipDir);
    }

    @Override
    public void storeFile(MultipartFile multipartFile) throws IOException {
        Resource resource = multipartFile.getResource();
        Path uploadedFilePath = baseDir.resolve(resource.getFilename());
        Files.copy(resource.getInputStream(), uploadedFilePath);
    }

    @Override
    public Stream<Path> getAllFiles() throws IOException {
        return Files.walk(baseDir).filter(path -> !path.equals(baseDir))
                .filter(path -> ! Files.isDirectory(path))
                .map(path -> baseDir.relativize(path));
    }

    @Override
    public Resource getFile(String fileName) throws FileNotFoundException {
        Path outputZip = tmpZipDir.resolve( FilenameUtils.getBaseName(fileName) + ".zip");
        Zipper.zip(baseDir.resolve(fileName).toString(), outputZip.toString());
        Resource resourceZipped = new InputStreamResource(new FileInputStream(outputZip.toFile()));
        return resourceZipped;
    }

    @Override
    public Resource getAllFilesZip() throws FileNotFoundException {
        Path outputZip = tmpZipDir.resolve(System.currentTimeMillis() + ".zip");
        Zipper.zip(baseDir.toString(), outputZip.toString());
        Resource resourceZipped = new InputStreamResource(new FileInputStream(outputZip.toFile()));
        return resourceZipped;
    }
}
