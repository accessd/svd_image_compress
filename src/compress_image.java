import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

/**
 * Класс compress_image - тип для сжатого изображения
 *
 */

public class compress_image {

    int numsingval; // кол-во "незануленных" сингулярных чисел

    int i, j = 0;

    int m; // количество строк

    int n; // количество столбцов

    double x_b = (double) 0x300;

    double x_s = (double) 0x2000;

    int type_image;

    double red_out[][]; // массивы для сжатого изображения

    double green_out[][]; //

    double blue_out[][]; //

    double red_in_u[][]; // массивы для исходного изображения

    double green_in_u[][]; //

    double blue_in_u[][]; //

    double red_v[][];

    double red_w[][];

    double green_v[][];

    double green_w[][];

    double blue_v[][];

    double blue_w[][];

    double work[][];

    private String filenamepath; //имя файла

    private BufferedImage uncompress_img; //расжатое изображение

    /**
     * @param filenamepath имя файла для сжатого изображения
     */
    compress_image(String filenamepath) {
        this.filenamepath = filenamepath;
    }

    /**
     * @return BufferedImage возвращает восстановленное изображение
     */
    public BufferedImage uncompress() {

        gui.setProgress(2);
        gui.setProgressText("read file...");
        //		 чтение файла содержащего сжатое изображение
        read_compress_file(filenamepath);

        gui.setProgress(3);
        gui.setProgressText("uncompress red...");
        //		 восстановление(A=U*W*T) компоненты RGB - red
        mulmatr(red_in_u, red_w, red_v, red_out);

        gui.setProgress(4);
        gui.setProgressText("uncompress green...");
        //		 восстановление(A=U*W*T) компоненты RGB - red
        mulmatr(green_in_u, green_w, green_v, green_out);

        gui.setProgress(5);
        gui.setProgressText("uncompress blue...");
        //      восстановление(A=U*W*T) компоненты RGB - red
        mulmatr(blue_in_u, blue_w, blue_v, blue_out);

        gui.setProgress(6);
        gui.setProgressText("set rgb...");
        //      создание изображения из red, green, blue(RGB)
        uncompress_img = setrgb(red_out, green_out, blue_out);

        gui.setProgress(7);
        gui.setProgressText("save image...");
        //      сохранение восстановленного изображения
        saveimage(uncompress_img, filenamepath);

        gui.setProgress(8);
        gui.setProgressText("image uncompressed!");

        return uncompress_img;

    }

    public void init_values() {
        System.out.println("m= " + m);
        System.out.println("n= " + n);
        red_out = new double[m][n]; // массивы для разсжатого изображения
        green_out = new double[m][n]; //
        blue_out = new double[m][n]; //

        red_in_u = new double[m][n]; // массивы для исходного изображения
        red_v = new double[n][n];
        red_w = new double[n][n];

        green_in_u = new double[m][n]; //
        green_v = new double[n][n];
        green_w = new double[n][n];

        blue_in_u = new double[m][n]; //
        blue_v = new double[n][n];
        blue_w = new double[n][n];

        work = new double[m][n];
    }

    /**
     * Чтение из файла массивов red, green, blue
     * @param filename_path имя файла для чтения
     */
    void read_compress_file(String filename_path) {

        try {
            RandomAccessFile dataIn = new RandomAccessFile(filename_path, "r");

            System.out.println(filename_path);

            m = dataIn.readShort(); // Чтение размеров исходного изображения
            // и кол-ва используемых
            n = dataIn.readShort(); // при сжатии сингулярных чисел
            numsingval = dataIn.readShort();//

            type_image = dataIn.readShort();
            init_values(); // Инициализация массивов

            for (i = 0; i < m; i++)
                for (j = 0; j < numsingval; j++) { // чтение массива red_u
                    //------------------------------------------------------------//
                    if (dataIn.readInt() == dataIn.getFilePointer() - 4) {
                        red_in_u[i][j] = dataIn.readShort();
                        red_in_u[i][j] /= x_s;
                    } else {
                        dataIn.seek(dataIn.getFilePointer() - 4);
                        red_in_u[i][j] = dataIn.readByte();
                        red_in_u[i][j] /= x_b;
                    }
                    //				 -------------------------------------------------------------//
                }
            for (i = 0; i < m; i++)
                for (j = 0; j < numsingval; j++) { // чтение массива green_u
                    //-------------------------------------------------------------//
                    if (dataIn.readInt() == dataIn.getFilePointer() - 4) {

                        green_in_u[i][j] = dataIn.readShort();
                        green_in_u[i][j] /= x_s;
                    } else {
                        dataIn.seek(dataIn.getFilePointer() - 4);
                        green_in_u[i][j] = dataIn.readByte();
                        green_in_u[i][j] /= x_b;
                    }
                }
            for (i = 0; i < m; i++)
                for (j = 0; j < numsingval; j++) { // чтение массива blue_u
                    //-------------------------------------------------------------//

                    if (dataIn.readInt() == dataIn.getFilePointer() - 4) {
                        blue_in_u[i][j] = dataIn.readShort();
                        blue_in_u[i][j] /= x_s;
                    } else {
                        dataIn.seek(dataIn.getFilePointer() - 4);
                        blue_in_u[i][j] = dataIn.readByte();
                        blue_in_u[i][j] /= x_b;
                    }
                    //-------------------------------------------------------------//
                }
            for (int i = 0; i < numsingval; i++)
                // чтение массива red_v
                for (int j = 0; j < n; j++) {
                    if (dataIn.readInt() == dataIn.getFilePointer() - 4) {
                        red_v[i][j] = dataIn.readShort();
                        red_v[i][j] /= x_s;
                    } else {
                        dataIn.seek(dataIn.getFilePointer() - 4);
                        red_v[i][j] = dataIn.readByte();
                        red_v[i][j] /= x_b;
                    }
                }
            for (int i = 0; i < numsingval; i++)
                // чтение массива green_v
                for (int j = 0; j < n; j++) {
                    if (dataIn.readInt() == dataIn.getFilePointer() - 4) {
                        green_v[i][j] = dataIn.readShort();
                        green_v[i][j] /= x_s;
                    } else {
                        dataIn.seek(dataIn.getFilePointer() - 4);
                        green_v[i][j] = dataIn.readByte();
                        green_v[i][j] /= x_b;
                    }
                }
            for (int i = 0; i < numsingval; i++)
                // чтение массива blue_v
                for (int j = 0; j < n; j++) {
                    if (dataIn.readInt() == dataIn.getFilePointer() - 4) {
                        blue_v[i][j] = dataIn.readShort();
                        blue_v[i][j] /= x_s;
                    } else {
                        dataIn.seek(dataIn.getFilePointer() - 4);
                        blue_v[i][j] = dataIn.readByte();
                        blue_v[i][j] /= x_b;
                    }
                }
            //			 чтение массивов red_w, green_w, blue_w
            for (int i = 0; i < numsingval; i++) {
                red_w[i][i] = dataIn.readDouble();
                green_w[i][i] = dataIn.readDouble();
                blue_w[i][i] = dataIn.readDouble();
            }
            dataIn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Восстановление массивов путем зануления диагональных элементов массивов w
        // столбцов массивов u, и строк массивов v

        for (int i = numsingval - 1; i < n; i++) {
            red_w[i][i] = 0;
            green_w[i][i] = 0;
            blue_w[i][i] = 0;
        }
        for (int i = 0; i < m; i++)
            for (int j = numsingval - 1; j < n; j++) {
                red_in_u[i][j] = 0;
                green_in_u[i][j] = 0;
                blue_in_u[i][j] = 0;
            }
        for (int i = numsingval - 1; i < n; i++)
            for (int j = 0; j < n; j++) {
                red_v[i][j] = 0;
                green_v[i][j] = 0;
                blue_v[i][j] = 0;
            }
    }

    /**
     * Перемножение трех матриц
     * @param u
     * @param w
     * @param v
     * @param mulmatr результат умножения
     */
    void mulmatr(double u[][], double w[][], double v[][], double mulmatr[][]) {
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
                work[i][j] = 0;
                for (int k = 0; k < n; k++)
                    work[i][j] += (u[i][k] * w[k][j]);
            }

        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
                mulmatr[i][j] = 0;
                for (int k = 0; k < n; k++)
                    mulmatr[i][j] += (work[i][k] * v[k][j]);
            }
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
                if (mulmatr[i][j] < 0)
                    mulmatr[i][j] = 0;
                if (mulmatr[i][j] > 255)
                    mulmatr[i][j] = 255;
                mulmatr[i][j] = Math.round(mulmatr[i][j]);
            }

    }

    private static final double[] EXPS = { 1, 10, 100, 1000, 10000, 100000,
            1000000, 10000000, 100000000, 1000000000, 10000000000l,
            100000000000l, 1000000000000l, 10000000000000l, 100000000000000l,
            1000000000000000l, 10000000000000000l, 100000000000000000l,
            1000000000000000000l };

    //	==============================================================

    /**
     * Метод остуществляет округление числа с заданной точностью.
     * @param arg - исходное число
     * @param prec -колл-во десятичных знаков после запятой
     * до которого производить округление
     * @return double округленное число
     */
    public static double round(final double arg, final int prec) {
        if (prec == 2) {
            if ((arg * 1000) % 10 == 5) {
                return roundImpl(arg - 2.2e-15, prec);
            }
        }
        return roundImpl(arg, prec);
    }

    //	=================================================================

    private static double roundImpl(final double arg, final int prec) {
        final double exp = EXPS[prec];
        return Math.round(arg * exp) / exp;
    }

    /**
     * @param red массив красной компоненты
     * @param green массив зеленой компоненты
     * @param blue массив синей компоненты
     * @return BufferedImage воосстановленное изображение
     */
    BufferedImage setrgb(double red[][], double green[][], double blue[][]) {
        BufferedImage image_1 = new BufferedImage(n, m, type_image);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                image_1.setRGB(i, j, ((int) red[j][i] << 16
                        | (int) green[j][i] << 8 | (int) blue[j][i]));

            }
        }
        return image_1;
    }

    public int getN_s() {
        return numsingval;
    }

    /**
     * Сохранение изображения
     * @param ImageForSave изображение для сохранения
     * @param FileName имя файла для сохранения изображения
     */
    void saveimage(BufferedImage ImageForSave, String FileName) {
        File f_1 = new File(FileName.substring(0, FileName.indexOf("."))
                + "_uncomp.bmp"); // имя файла для записи
        try {
            ImageIO.write(ImageForSave, "bmp", f_1);
        } catch (Exception ex) {
            System.out.println("Exception in save image!");
        }
        ;
    }
}
