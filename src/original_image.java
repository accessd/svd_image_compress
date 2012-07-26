import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

/**
 * Класс original_image - тип для исходного изображения 
 *
 */
public class original_image {

    static double machineepsilon = 5E-16; // константа - машинная точность

    static int maxsvditerations = 30; // константа - количество итераций в svd разложении

    int i, j = 0;

    int m; // количество строк

    int n; // количество столбцов

    double x_b = (double) 0x300;

    double x_s = (double) 0x2000;

    int type_image; // тип изображения

    double red_in_u[][]; // массивы для исходного изображения

    double green_in_u[][]; //

    double blue_in_u[][]; //

    double red_v[][]; //

    double red_w[][]; //

    double green_v[][]; //

    double green_w[][]; //

    double blue_v[][]; //

    double blue_w[][]; //

    BufferedImage orig_img; // исходное изображение типа BufferedImage

    original_image(int ih, int iw) {

        m = ih; // инициализация размеров матриц исходя из размеров изображения
        n = iw; //
        //		 инициализация массивов для исходного изображения
        red_in_u = new double[m][n];
        red_v = new double[n][n];
        red_w = new double[n][n];

        green_in_u = new double[m][n]; //
        green_v = new double[n][n];
        green_w = new double[n][n];

        blue_in_u = new double[m][n]; //
        blue_v = new double[n][n];
        blue_w = new double[n][n];
    }

    public int set_compress_coeff(int comp_coeff) {
        //		вычисление коэффициента компресии для byte
        //return (m*n/(comp_coeff*(m + n + 16)));
        return (m * n / (2 * comp_coeff * (m + n + 4) - 64)); //для short
    }

    //основной метод, производит сжатие изображения
    public void compress(File image_filename, int num_sing_val) {

        gui.setProgress(1);
        gui.setProgressText("open image...");
        open_image(image_filename);

        gui.setProgress(2);
        gui.setProgressText("get rgb...");
        getrgb(orig_img, red_in_u, green_in_u, blue_in_u);

        gui.setProgress(3);
        gui.setProgressText("svd red...");
        svd(red_in_u, red_w, red_v); // svd разложение массива red_in_u

        gui.setProgress(4);
        gui.setProgressText("svd green...");
        svd(green_in_u, green_w, green_v); // svd разложение массива green_in_u

        gui.setProgress(5);
        gui.setProgressText("svd blue...");
        svd(blue_in_u, blue_w, blue_v); // svd разложение массива blue_in_u

        gui.setProgress(6);
        gui.setProgressText("transpose matrix...");
        transpose(red_v); // транспонирование матриц v
        transpose(green_v); //
        transpose(blue_v); //

        gui.setProgress(7);
        gui.setProgressText("save file...");
        save_compress_image_in_file(red_in_u, red_v, red_w, green_in_u,
                green_v, green_w, blue_in_u, blue_v, blue_w, image_filename
                .getAbsolutePath(), num_sing_val);

        gui.setProgress(8);
        gui.setProgressText("image compressed!");

        sing(num_sing_val);

    }

    //открывает изображение из файла
    public void open_image(File image_filename) {
        try {
            orig_img = ImageIO.read(image_filename);
        } catch (IOException e) {
            System.err.println("Image not load!");
        }
    }

    //разложение изображения на основные цвета
    void getrgb(BufferedImage image, double red[][], double green[][],
                double blue[][]) {

        type_image = image.getType();

        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++) {
                red[j][i] = ((image.getRGB(i, j) >> 16) & 0xff);
                green[j][i] = ((image.getRGB(i, j) >> 8) & 0xff);
                blue[j][i] = (image.getRGB(i, j) & 0xff);
            }
    }

    void sing(int n_sign) { //вычисление вклада первых k сингулярных чисел
        double p = 0; //число, показывающее отношение суммы первых n_sign сингулярных
        //чисел к сумме всех сингулярных чисел
        double p1 = 0; //сумма всех сингулярных чисел
        double p2 = 0; //сумма первых n_sign чисел

        for (int i = 0; i < n; i++) {
            p1 += red_w[i][i] + green_w[i][i] + blue_w[i][i];
        }

        for (int i = 0; i < n_sign; i++) {
            p2 += red_w[i][i] + green_w[i][i] + blue_w[i][i];
        }

        p = p2 / p1;

        System.out.println("p = " + p);

    }

    //	 запись массивов в файл
    void save_compress_image_in_file(double red_u[][], double red_v[][],
                                     double red_w[][], double green_u[][], double green_v[][],
                                     double green_w[][], double blue_u[][], double blue_v[][],
                                     double blue_w[][], String filename, int num_sing_val) {

        try {
            boolean success = (new File(filename + ".svd")).delete();
            if (!success) {
                // Deletion failed
                System.out.println("Deletion failed!");
            }
            RandomAccessFile dataOut = new RandomAccessFile(filename + ".svd",
                    "rw");
            FileWriter file = new FileWriter("test");

            dataOut.writeShort(m); // Запись размеров изображения и
            dataOut.writeShort(n); // кол-ва использованных сингулярных значений
            dataOut.writeShort(num_sing_val);//
            dataOut.writeShort(type_image);

            for (int i = 0; i < m; i++)
                // запись массива red_u
                for (int j = 0; j < num_sing_val; j++) {
                    //--------------------------------------------------------------------------//
                    if ((red_u[i][j] * x_b) > 127 || (red_u[i][j] * x_b) < -127) {
                        dataOut.writeInt((int) dataOut.getFilePointer());
                        dataOut.writeShort((int) (red_u[i][j] * x_s));
                    } else
                        dataOut.writeByte((byte) (red_u[i][j] * x_b));
                    //file.write(red_u[i][i] + "\n");

                }

            for (int i = 0; i < m; i++)
                // запись массива green_u
                for (int j = 0; j < num_sing_val; j++) {
                    //--------------------------------------------------------------------------//
                    if ((green_u[i][j] * x_b) > 127
                            || (green_u[i][j] * x_b) < -127) {
                        dataOut.writeInt((int) dataOut.getFilePointer());
                        dataOut.writeShort((int) (green_u[i][j] * x_s));
                    } else
                        dataOut.writeByte((byte) (green_u[i][j] * x_b));
                    //file.write(green_u[i][i] + "\n");
                }

            for (int i = 0; i < m; i++)
                // запись массива blue_u
                for (int j = 0; j < num_sing_val; j++) {
                    //--------------------------------------------------------------------------//
                    if ((blue_u[i][j] * x_b) > 127
                            || (blue_u[i][j] * x_b) < -127) {
                        dataOut.writeInt((int) dataOut.getFilePointer());
                        dataOut.writeShort((int) (blue_u[i][j] * x_s));
                    } else
                        dataOut.writeByte((byte) (blue_u[i][j] * x_b));
                    //file.write(blue_u[i][i] + "\n");

                    //--------------------------------------------------------------------------//
                }
            for (int i = 0; i < num_sing_val; i++)
                // запись массива red_v
                for (int j = 0; j < n; j++) {
                    if ((red_v[i][j] * x_b) > 127 || (red_v[i][j] * x_b) < -127) {
                        dataOut.writeInt((int) dataOut.getFilePointer());
                        dataOut.writeShort((int) (red_v[i][j] * x_s));
                    } else
                        dataOut.writeByte((byte) (red_v[i][j] * x_b));
                    //file.write(red_v[i][i] + "\n");
                }
            for (int i = 0; i < num_sing_val; i++)
                // запись массива green_v
                for (int j = 0; j < n; j++) {
                    if ((green_v[i][j] * x_b) > 127
                            || (green_v[i][j] * x_b) < -127) {
                        dataOut.writeInt((int) dataOut.getFilePointer());
                        dataOut.writeShort((int) (green_v[i][j] * x_s));
                    } else
                        dataOut.writeByte((byte) (green_v[i][j] * x_b));
                    //file.write(green_v[i][i] + "\n");
                }
            for (int i = 0; i < num_sing_val; i++)
                // запись массива blue_v
                for (int j = 0; j < n; j++) {
                    if ((blue_v[i][j] * x_b) > 127
                            || (blue_v[i][j] * x_b) < -127) {
                        dataOut.writeInt((int) dataOut.getFilePointer());
                        dataOut.writeShort((int) (blue_v[i][j] * x_s));
                    } else
                        dataOut.writeByte((byte) (blue_v[i][j] * x_b));
                    //file.write(blue_v[i][i] + "\n");
                }
            for (int i = 0; i < num_sing_val; i++) { // запись массивов w
                dataOut.writeDouble(red_w[i][i]);
                dataOut.writeDouble(green_w[i][i]);
                dataOut.writeDouble(blue_w[i][i]);
            }
            for (int i = 0; i < n; i++) {
                file.write(red_w[i][i] + "\n");
                file.write(blue_w[i][i] + "\n");
                file.write(green_w[i][i] + "\n");
            }
            dataOut.close();
            file.close();

        } catch (Exception ex) {
            System.out.println("Exception in save matrix in file!");
            ex.printStackTrace();
        }

    }

    void transpose(double A[][]) //транспонирует матрицу
    {
        double buf;
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++) {
                buf = A[i][j];
                A[i][j] = A[j][i];
                A[j][i] = buf;
            }
    }

    /**Алгоритм сингулярного разложения матрицы размером MxN.

     Алгоритм принимает матрицу A и приводит её к виду: A = U*W*Transpone(V).
     Матрица U имеет размер MxN, матрица W - диагональная,  матрица  V  орто-
     нормированная.

     * @param a Матрица А, строки с 1 по M, столбцы с 1 по N
     * Результат работы помещается:
     * Матрица U замещает матрицу A, строки с 1 по M, столбцы с 1 по N.
     * @param w Диагональ матрицы W хранится в переменной W (матрица NxN).
     * @param v Матрица V (не транспонированная) хранится в переменной V. Строки
    с 1 по N, столбцы с 1 по N.
     */
    public static void svd(double[][] a, double[][] w, double[][] v) {
        // переменные используемые для осуществления итераций
        int i, its, j, jj, k, l = 0, nm = 0;
        // флаг достижения SVD разложения за указанное количество итераций
        boolean flag;
        int m = a.length; // размерность массивов (количество строк)
        int n = a[0].length; // размерность массивов (количество строк, столбцов)
        double c; // значение cos при преобразовании
        double s; // значение sin при преобразовании
        double z; // значение сдвига при QR преобразовании
        double x, y; // используется в QR преобразовании
        double f, h; // коэффициенты вращения Гивенса
        double[] rv1 = new double[n]; // промежуточный массив
        double anorm = 0.; // норма столбца при преобразовании Хаусхолдера
        double g = 0.; // собственное значение при преобразовании Хааусхолдера
        double scale = 0.;

        //Хаусхолдерово приведения к двухдиагональной форме
        for (i = 0; i < n; i++) {
            l = i + 1;
            rv1[i] = scale * g;
            g = s = scale = 0.;
            if (i < m) {
                for (k = i; k < m; k++)
                    scale += abs(a[k][i]);
                if (scale != 0.0) {
                    for (k = i; k < m; k++) {
                        a[k][i] /= scale;
                        s += a[k][i] * a[k][i];
                    }
                    f = a[i][i];
                    g = -SIGN(Math.sqrt(s), f);// вычисление собственного значения
                    h = f * g - s;
                    a[i][i] = f - g;
                    //if (i!=(n-1)) {		// CHECK
                    for (j = l; j < n; j++) {
                        // вычисление отражений Хаусхолдера
                        for (s = 0, k = i; k < m; k++)
                            s += a[k][i] * a[k][j];
                        f = s / h;
                        for (k = i; k < m; k++)
                            a[k][j] += f * a[k][i];
                    }
                    //}
                    for (k = i; k < m; k++)
                        a[k][i] *= scale;
                }
            }
            w[i][i] = scale * g;
            g = s = scale = 0.0;
            if (i < m && i != n - 1) { //
                for (k = l; k < n; k++)
                    scale += abs(a[i][k]);
                if (scale != 0.) {
                    for (k = l; k < n; k++) { //
                        a[i][k] /= scale;
                        s += a[i][k] * a[i][k];
                    }
                    f = a[i][l];
                    g = -SIGN(Math.sqrt(s), f);
                    h = f * g - s;
                    a[i][l] = f - g;
                    for (k = l; k < n; k++)
                        rv1[k] = a[i][k] / h;
                    if (i != m - 1) { //
                        for (j = l; j < m; j++) { //
                            for (s = 0, k = l; k < n; k++)
                                s += a[j][k] * a[i][k];
                            for (k = l; k < n; k++)
                                a[j][k] += s * rv1[k];
                        }
                    }
                    for (k = l; k < n; k++)
                        a[i][k] *= scale;
                }
            } //i<m && i!=n-1
            anorm = Math.max(anorm, (abs(w[i][i]) + abs(rv1[i])));
        } //i
        //Накопление правосторонних преобразований
        for (i = n - 1; i >= 0; --i) {
            if (i < n - 1) { //
                if (g != 0.) {
                    for (j = l; j < n; j++)
                        v[j][i] = (a[i][j] / a[i][l]) / g;//Двойное деление обходит
                    //возможный машинный ноль
                    for (j = l; j < n; j++) {
                        for (s = 0, k = l; k < n; k++)
                            s += a[i][k] * v[k][j];
                        for (k = l; k < n; k++)
                            v[k][j] += s * v[k][i];
                    }
                }
                for (j = l; j < n; j++)
                    //
                    v[i][j] = v[j][i] = 0.0;
            }
            v[i][i] = 1.0;
            g = rv1[i];
            l = i;
        }
        //for (i=IMIN(m,n);i>=1;i--) {	// !
        //for (i = n-1; i>=0; --i)  {
        //Накопление левосторонних преобразований
        for (i = Math.min(m - 1, n - 1); i >= 0; --i) {
            l = i + 1;
            g = w[i][i];
            if (i < n - 1) //
                for (j = l; j < n; j++)
                    //
                    a[i][j] = 0.0;
            if (g != 0.) {
                g = 1. / g;
                if (i != n - 1) {
                    for (j = l; j < n; j++) {
                        for (s = 0, k = l; k < m; k++)
                            s += a[k][i] * a[k][j];
                        f = (s / a[i][i]) * g;
                        for (k = i; k < m; k++)
                            a[k][j] += f * a[k][i];
                    }
                }
                for (j = i; j < m; j++)
                    a[j][i] *= g;
            } else {
                for (j = i; j < m; j++)
                    a[j][i] = 0.0;
            }
            a[i][i] += 1.0;
        }
        //Диагонализация двухдиагональной формы
        for (k = n - 1; k >= 0; --k) {
            for (its = 1; its <= 50; ++its) {
                flag = true;
                //Проверка возможности расщепления для l=k с шагом -1 до 0 выполнить
                for (l = k; l >= 0; --l) {
                    nm = l - 1;
                    if ((abs(rv1[l]) + anorm) == anorm) {//rv[l] всегда равно нулю,
                        //поэтому выхода через конец цикла не будет
                        flag = false;
                        break;
                    }
                    if ((abs(w[nm][nm]) + anorm) == anorm)
                        break;
                }
                if (flag) {
                    //Если L больше чем I, то rv1[L] присваивается нулевое значение
                    c = 0.0;
                    s = 1.0;
                    for (i = l; i <= k; i++) { //
                        f = s * rv1[i];
                        rv1[i] = c * rv1[i];
                        if ((abs(f) + anorm) == anorm)
                            break;
                        g = w[i][i];
                        h = pythag(f, g);
                        w[i][i] = h;
                        h = 1.0 / h;
                        c = g * h;
                        s = -f * h;
                        for (j = 0; j < m; j++) {
                            y = a[j][nm];
                            z = a[j][i];
                            a[j][nm] = y * c + z * s;
                            a[j][i] = z * c - y * s;
                        }
                    }
                } //flag
                //Проверка сходимости
                z = w[k][k];
                if (l == k) {
                    //Сдвиг выбирается из нижнего углового минора порядка 2
                    if (z < 0.) {
                        w[k][k] = -z;
                        for (j = 0; j < n; j++)
                            v[j][k] = -v[j][k];
                    }
                    break;
                } //l==k
                x = w[l][l];
                nm = k - 1;
                y = w[nm][nm];
                g = rv1[nm];
                h = rv1[k];
                f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2 * h * y);
                g = pythag(f, 1.0);
                f = ((x - z) * (x + z) + h * ((y / (f + SIGN(g, f))) - h)) / x;
                //Следующее QR-преобразование
                c = s = 1.0;
                for (j = l; j <= nm; j++) {
                    i = j + 1;
                    g = rv1[i];
                    y = w[i][i];
                    h = s * g;
                    g = c * g;
                    z = pythag(f, h);
                    rv1[j] = z;
                    c = f / z;
                    s = h / z;
                    f = x * c + g * s;
                    g = g * c - x * s;
                    h = y * s;
                    y *= c;
                    for (jj = 0; jj < n; jj++) {
                        x = v[jj][j];
                        z = v[jj][i];
                        v[jj][j] = x * c + z * s;
                        v[jj][i] = z * c - x * s;
                    }
                    z = pythag(f, h);
                    w[j][j] = z;
                    //Вращение может быть произвольным, если z равно нулю
                    if (z != 0.0) {
                        z = 1.0 / z;
                        c = f * z;
                        s = h * z;
                    }
                    f = c * g + s * y;
                    x = c * y - s * g;
                    for (jj = 0; jj < m; ++jj) {
                        y = a[jj][j];
                        z = a[jj][i];
                        a[jj][j] = y * c + z * s;
                        a[jj][i] = z * c - y * s;
                    }
                } //j<nm
                rv1[l] = 0.0;
                rv1[k] = f;
                w[k][k] = x;
            } //its
        } //k
        // free rv1
    }

    static final double abs(double a) {
        return (a < 0.) ? -a : a;
    }

    static final double pythag(double a, double b) {
        return Math.sqrt(a * a + b * b);
    }

    static final double SIGN(double a, double b) {
        return ((b) >= 0. ? abs(a) : -abs(a));
    }

    //----------------------------------------------------------------

}
