
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.security.MessageDigest;
import java.security.DigestInputStream;

public class ChecksumTool {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java ChecksumTool <archivo>");
            return;
        }

        Path filePath = Path.of(args[0]);
        if (!Files.exists(filePath)) {
            System.out.println("Archivo no encontrado: " + filePath);
            return;
        }

        try {
            long size = Files.size(filePath);
            long crc32 = calcularCRC32(filePath);
            String sha256 = calcularSHA256(filePath);

            System.out.println("Archivo: " + filePath.getFileName());
            System.out.println("Tamaño: " + size + " bytes");
            System.out.println("CRC32 (update4j): " + crc32);
            System.out.println("SHA-256: " + sha256);

        } catch (Exception e) {
            System.err.println("Error al calcular checksum:");
            e.printStackTrace();
        }
    }

    private static long calcularCRC32(Path path) throws IOException {
        CRC32 crc = new CRC32();
        try (CheckedInputStream cis = new CheckedInputStream(new FileInputStream(path.toFile()), crc)) {
            byte[] buffer = new byte[4096];
            while (cis.read(buffer) >= 0) {
                // Solo se lee para actualizar el CRC
            }
        }
        return crc.getValue();
    }

    private static String calcularSHA256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(path.toFile()), digest)) {
            byte[] buffer = new byte[4096];
            while (dis.read(buffer) >= 0) {
                // Solo leer
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hex = new StringBuilder();
        for (byte b : hashBytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
