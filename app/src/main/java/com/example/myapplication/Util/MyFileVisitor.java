package com.example.myapplication.Util;

import com.example.myapplication.Entry.Song;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class MyFileVisitor implements FileVisitor<Path> {

    private List<Song> songs;

    public MyFileVisitor(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        System.out.println("pre visit dir:" + dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        System.out.println("visit file: " + file);
        String name = file.getFileName().toString();
        if (name.endsWith(".mp3")){
            String[] strings = name.split("/");
            Song song = new Song(strings[strings.length-1],file);
            songs.add(song);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.out.println("visit file failed: " + file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        System.out.println("post visit directory: " + dir);
        return FileVisitResult.CONTINUE;
    }
}
