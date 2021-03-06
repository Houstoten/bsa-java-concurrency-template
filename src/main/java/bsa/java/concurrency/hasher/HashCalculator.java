package bsa.java.concurrency.hasher;

import bsa.java.concurrency.exception.ImageBrokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class HashCalculator implements Hasher {

    @Qualifier("asyncForMethods")
    @Autowired
    private Executor executor;

    @Async("asyncForMethods")
    public CompletableFuture<Long> diagonalHash(byte[] image) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Image img = ImageIO.read(new ByteArrayInputStream(image))
                        .getScaledInstance(9, 9, Image.SCALE_SMOOTH);
                BufferedImage imgGray = new BufferedImage(9
                        , 9
                        , BufferedImage.TYPE_BYTE_GRAY);
                Graphics g = imgGray.getGraphics();
                g.drawImage(img, 0, 0, null);
                g.dispose();
                long hash = 0;
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if ((imgGray.getRGB(i + 1, j + 1) & 0b11111111)
                                > (imgGray.getRGB(i, j) & 0b11111111)) {
                            hash |= 1;
                        }
                        hash <<= 1;
                    }
                }
                return hash;
            } catch (IOException e) {
                throw new ImageBrokenException();
            }
        }, executor);
    }
}
