package gilli.files.storage;

import gilli.files.FileChecksum;
import gilli.internal.main.Gilli;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileDupFinder
{
    public static void findDup(List<String> parentPaths, boolean delete) throws Exception
    {
        FileDupContext context = new FileDupContext();
        context.makeFresh();
        context.delete = delete;

        FileVisitorImpl visitor = new FileVisitorImpl(context);

        for (String parentPath : parentPaths)
            Files.walkFileTree(Paths.get(parentPath).toFile().getCanonicalFile().toPath(), visitor);

        context.walkingDone();
        calc(context);
        visitor.cleanUp();
        Gilli.stdout.info("{} duplicate Set(s) found", context.dupId);
        Gilli.stdout.info("{} MB will be saved, if duplicate files are deleted", context.saveableSize / 1024.0 / 1024.0);

        context.clear();
    }

    private static void calc(FileDupContext context)
    {
        //System.out.println("sizes count " + context.sizeWiseFileNames.size());
        for (Map.Entry<Long, List<File>> e : context.sizeWiseFileNames.entrySet())
        {
            if (e.getValue().size() == 1)
                continue;

            if (e.getKey() > 1_000_000_000) //> 1GB
            {
                String filePaths = e.getValue().stream().map(File::getAbsolutePath).collect(Collectors.joining(" | "));
                Gilli.stdout.info("File Greater than 1GB. Hence Skipping hashing.... : {} bytes : {}", e.getKey(), filePaths);
            }

            Map<String, List<File>> hashWiseFiles = new HashMap<>();

            for (File file : e.getValue())
            {
                String checksum = FileChecksum.md5(file.getAbsolutePath());
                hashWiseFiles.putIfAbsent(checksum, new ArrayList<>());
                hashWiseFiles.get(checksum).add(file);
            }

            for (Map.Entry<String, List<File>> hashWise : hashWiseFiles.entrySet())
            {
                if (hashWise.getValue().size() > 1)
                    dupFound(context, hashWise.getValue());
            }
        }
    }

    private static void dupFound(FileDupContext context, List<File> files)
    {
        long dupId = context.nextDupId();
        for (File file : files)
            Gilli.stdout.info("{}-DUP: {}:{}", dupId, file.length(), file);

        for (int i = 1; i < files.size(); i++)
            context.addToSaveableSize(files.get(i).length());

        if (context.delete)
        {
            for (int i = 1; i < files.size(); i++)
            {
                //Gilli.stdout.info("{}-DUP: Deleting... {}", dupId, files.get(i));
                boolean success = files.get(i).delete();
                Gilli.stdout.info("{}-DUP: Deletion res: {}: Deletion  {}", dupId, success ? "Deleted" : "Failed", files.get(i));
            }
        }
    }

    public static class FileDupContext
    {
        public File tmpDir = new File(System.getProperty("java.io.tmpdir"), "gilli-filedup-" + System.currentTimeMillis());
        public File sizeFile = new File(tmpDir, "size.txt");

        public Map<Long, List<File>> sizeWiseFileNames = new HashMap<>();

        public boolean delete;

        public long dupId = 0;

        public long visitedFileCount = 0;

        public double saveableSize = 0;
        public double deletedSize = 0;

        public void addToSaveableSize(double size)
        {
            saveableSize += size;
        }

        public void addToDeletedSize(double size)
        {
            deletedSize += size;
        }

        public long nextDupId()
        {
            return ++dupId;
        }

        public void walkingDone()
        {
            System.out.println();
            System.out.println("Totally Visited Files : " + visitedFileCount);
        }

        public void addFileSize(File file)
        {
            visitedFileCount++;

            if (visitedFileCount % 100 == 0)
                System.out.print(visitedFileCount);
            else if (visitedFileCount % 10 == 0)
                System.out.print(".");

            //System.out.println(file.length() + "-" + file);
            sizeWiseFileNames.putIfAbsent(file.length(), new ArrayList<>());
            sizeWiseFileNames.get(file.length()).add(file);
        }

        public void makeFresh() throws Exception
        {
            //boolean dirDone = tmpDir.mkdirs();
            //Gilli.stdout.info("Created tmp dir: {}", tmpDir);

            //boolean sizeFileCreated = this.sizeFile.createNewFile();
            //Gilli.stdout.info("Created sizeFile: {}", sizeFileCreated);
        }
        public void clear()
        {
            //sizeFile.delete();
            //tmpDir.delete();
        }
    }

    private static class FileVisitorImpl extends SimpleFileVisitor<Path>
    {
        private final FileDupContext context;

        public FileVisitorImpl(FileDupContext context) throws Exception
        {
            this.context = context;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
            File cur = file.toFile();

            if (!cur.isFile())
                return FileVisitResult.CONTINUE;

            context.addFileSize(cur);

            return FileVisitResult.CONTINUE;
        }

        public void cleanUp()
        {
        }
    }

}
