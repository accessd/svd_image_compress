import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;

public class gui extends JFrame implements ActionListener, SwingConstants {

    private static final long serialVersionUID = 1L;

    {
        //Set Look & Feel
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager
                    .getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel topPanel; // верхняя панель

    private JPanel botPanel; // нижняя панель

    private picturePanel topImagePanel; // верхняя панель для исходного изображения

    private JPanel topRigthPanel; // верхняя правая панель

    private picturePanel botImagePanel; // нижняя панель для сжатого изображения

    private JPanel botRigthPanel; // нижняя правая панель

    private JLabel lblSizeImage; // размер изображения

    private JLabel lblSizeFile; // размер файла исходного изображения

    private JLabel lblNumSingVal; // кол-во сингулярных чисел в исходном изображении

    private JTextField jtfNumSingVal; // ввод кол-ва синг.чисел для сжатия

    private JLabel lblCompressCoeff; // коэфф.компрессии

    private JLabel lbl1;

    private JButton btnComp; // кнопка для сжатия изображения

    private JLabel lblSizeImage1; // размер распакуемого изображения

    private JLabel lblSizeFile1; // размер файла для сжатого изображения

    private JLabel lblNumSingVal1; // кол-во синг.чисел при рассжатии

    private JButton btnUnComp; // кнопка для осуществления рассжатия

    private JPanel progressPanel; // панель для прогресс бара

    private static JProgressBar progress; // прогресс

    private static JLabel lblProgress; // label для прогресса

    private JMenuBar menu; // меню

    private JMenu menuOpen; // меню Открыть

    private JMenuItem menuItemOpen; // пункт меню Открыть

    private JMenuItem menuItemExit; // пункт меню Выход

    private JFileChooser jc = new JFileChooser(); // диалог открытия файла изображения

    private JFileChooser jc_comp = new JFileChooser(); // диалог открытия файла сжатого изображения

    private String dir; // директория до файла

    private String filenamepath; // путь до файла изображения

    private String compress_filename_path; // путь до файла сжатого изображения

    private long compress_file_size; // размер файла сжатого изображения

    private long original_file_size; // размер файла исходного изображения

    private BufferedImage img, uncompress_img; // исходное и рассжатое изображения

    private File f; // файл

    public gui() {
        createGUI();
        setSize(400, 400);
        setTitle("Compress image with help SVD");
        this.setResizable(false);
        pack();
        setLocationByPlatform(true);
        Image icon = Toolkit.getDefaultToolkit().getImage("icon.gif");
        setIconImage(icon);
    }

    private void createGUI() {

        // создание меню
        menu = new JMenuBar();
        this.setJMenuBar(menu);

        menuOpen = new JMenu("File");
        menu.add(menuOpen);

        menuItemOpen = new JMenuItem("Open image...");
        menuItemExit = new JMenuItem("Exit");
        menuItemOpen.addActionListener(this);
        menuItemExit.addActionListener(this);
        menuOpen.add(menuItemOpen);
        menuOpen.add(menuItemExit);

        // создание основных компонентов формы
        JPanel c = new JPanel(); // панель (клиентская часть окна)
        getContentPane().add(c);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); // установка компоновщика для клиентско части окна
        c.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // установка бордюра

        topPanel = new JPanel(); // создание верхней панели окна
        topPanel.setPreferredSize(new Dimension(472, 256)); // установка размера
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS)); // установка компоновщика
        topPanel.setBorder(BorderFactory.createTitledBorder("Compress")); // создание бордюра

        topImagePanel = new picturePanel();
        topImagePanel.setPreferredSize(new Dimension(240, 220));
        topImagePanel.setMinimumSize(new Dimension(240, 220));
        topImagePanel.setMaximumSize(new Dimension(240, 220));
        topImagePanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0,
                0)));
        topPanel.add(topImagePanel);

        topPanel.add(Box.createHorizontalStrut(12));

        topRigthPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        topRigthPanel.setLayout(gbl);

        GridBagConstraints cs = new GridBagConstraints();
        cs.insets = new Insets(5, 5, 5, 5);

        lblSizeImage = new JLabel("Size of image:        ");
        cs.gridwidth = GridBagConstraints.REMAINDER;
        cs.fill = GridBagConstraints.BOTH;
        cs.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(lblSizeImage, cs);
        topRigthPanel.add(lblSizeImage);

        lblSizeFile = new JLabel("Size of file:           ");
        cs.gridwidth = GridBagConstraints.REMAINDER;
        cs.fill = GridBagConstraints.BOTH;
        cs.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(lblSizeFile, cs);
        topRigthPanel.add(lblSizeFile);

        lblNumSingVal = new JLabel("Number of sing val:     ");
        cs.gridwidth = GridBagConstraints.REMAINDER;
        cs.fill = GridBagConstraints.BOTH;
        cs.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(lblNumSingVal, cs);
        topRigthPanel.add(lblNumSingVal);

        lbl1 = new JLabel("Set number of sing val:");
        cs.gridwidth = GridBagConstraints.RELATIVE;
        cs.fill = GridBagConstraints.BOTH;
        gbl.setConstraints(lbl1, cs);
        topRigthPanel.add(lbl1);

        jtfNumSingVal = new JTextField();
        cs.gridwidth = GridBagConstraints.REMAINDER;
        cs.ipadx = 50;
        cs.fill = GridBagConstraints.BOTH;
        cs.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(jtfNumSingVal, cs);
        topRigthPanel.add(jtfNumSingVal);

        lblCompressCoeff = new JLabel("Compress coeff:      ");
        cs.gridwidth = GridBagConstraints.REMAINDER;
        cs.fill = GridBagConstraints.BOTH;
        cs.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(lblCompressCoeff, cs);
        topRigthPanel.add(lblCompressCoeff);

        btnComp = new JButton("Compress");
        btnComp.setEnabled(false);
        btnComp.setPreferredSize(new Dimension(60, 25));
        btnComp.setMaximumSize(new Dimension(60, 25));
        btnComp.setMinimumSize(new Dimension(60, 25));
        cs.insets = new Insets(17, 5, 5, 5);
        cs.fill = GridBagConstraints.CENTER;
        cs.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(btnComp, cs);
        btnComp.addActionListener(this);
        topRigthPanel.add(btnComp);

        topPanel.add(topRigthPanel);

        topPanel.add(Box.createVerticalStrut(12));

        botPanel = new JPanel();
        botPanel.setPreferredSize(new Dimension(472, 256));
        botPanel.setMaximumSize(new Dimension(472, 256));
        botPanel.setMinimumSize(new Dimension(472, 256));
        botPanel.setLayout(new BoxLayout(botPanel, BoxLayout.X_AXIS));
        botPanel.setBorder(BorderFactory.createTitledBorder("Uncompress"));

        botImagePanel = new picturePanel();
        botImagePanel.setPreferredSize(new Dimension(240, 220));
        botImagePanel.setMinimumSize(new Dimension(240, 220));
        botImagePanel.setMaximumSize(new Dimension(240, 220));
        botImagePanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0,
                0)));
        botPanel.add(botImagePanel);

        botPanel.add(Box.createHorizontalStrut(12));

        botRigthPanel = new JPanel();
        botRigthPanel.setPreferredSize(new Dimension(205, 230));
        botRigthPanel.setMinimumSize(new Dimension(205, 230));
        botRigthPanel.setMaximumSize(new Dimension(205, 230));
        botRigthPanel.setLayout(gbl);

        GridBagConstraints cs1 = new GridBagConstraints();
        cs1.insets = new Insets(5, 5, 5, 5);

        lblSizeImage1 = new JLabel("Size of image:        ");
        cs1.gridwidth = GridBagConstraints.REMAINDER;
        cs1.fill = GridBagConstraints.BOTH;
        cs1.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(lblSizeImage1, cs1);
        botRigthPanel.add(lblSizeImage1);

        lblSizeFile1 = new JLabel("Size of file:        ");
        cs1.gridwidth = GridBagConstraints.REMAINDER;
        cs1.fill = GridBagConstraints.BOTH;
        cs1.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(lblSizeFile1, cs1);
        botRigthPanel.add(lblSizeFile1);

        lblNumSingVal1 = new JLabel("Number of sing val:      ");
        cs1.gridwidth = GridBagConstraints.REMAINDER;
        cs1.fill = GridBagConstraints.BOTH;
        cs1.anchor = GridBagConstraints.EAST;
        gbl.setConstraints(lblNumSingVal1, cs1);
        botRigthPanel.add(lblNumSingVal1);

        btnUnComp = new JButton("Uncompress");
        btnUnComp.setMinimumSize(new Dimension(60, 25));
        cs1.insets = new Insets(17, 5, 5, 5);
        cs1.fill = GridBagConstraints.CENTER;
        cs1.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(btnUnComp, cs1);
        btnUnComp.addActionListener(this);
        botRigthPanel.add(btnUnComp);

        botPanel.add(botRigthPanel);

        progressPanel = new JPanel();
        progressPanel.setLayout(gbl);
        GridBagConstraints cs2 = new GridBagConstraints();
        cs2.insets = new Insets(1, 1, 1, 1);

        lblProgress = new JLabel("       ");
        lblProgress.setHorizontalAlignment(CENTER);
        cs2.gridwidth = GridBagConstraints.REMAINDER;
        cs2.fill = GridBagConstraints.BOTH;
        cs2.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(lblProgress, cs2);
        progressPanel.add(lblProgress);

        progress = new JProgressBar();
        progress.setMaximum(8);
        progress.setPreferredSize(new Dimension(200, 15));
        gbl.setConstraints(lblProgress, cs2);
        progressPanel.add(progress);

        c.add(topPanel);
        c.add(botPanel);
        c.add(progressPanel);

    }

    public static void setProgress(int pr) {
        progress.setValue(pr);
    }

    public static void setProgressText(String lbl_str) {
        lblProgress.setText(lbl_str);
    }

    class Previewer extends JPanel implements PropertyChangeListener {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private picturePanel pic;

        public Previewer() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(200, 200));
            pic = new picturePanel();
            pic.setBorder(new LineBorder(null));
            add(pic);
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(
                    JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                if (e.getNewValue() != null) {
                    pic.setImageFile(jc.getSelectedFile());
                }
            }
        }
    }

    public void actionPerformed(ActionEvent e) { //
        if (e.getSource().equals(menuItemOpen)) {
            // создание диагога "Open image...":
            class BMPFileFilter extends FileFilter {

                public boolean accept(File file) {
                    if (file.isDirectory())
                        return true;
                    return (file.getName().endsWith("bmp") || file.getName()
                            .endsWith("BMP"));
                }

                public String getDescription() {
                    return "Image (*.bmp)";
                }
            }

            Previewer previewer = new Previewer();
            jc.addPropertyChangeListener(previewer);
            jc.setDialogTitle("Open image...");
            jc.setFileFilter(new BMPFileFilter());
            jc.setAccessory(previewer);

            if (new File("H:\\tempPictures\\").exists())
                jc.setCurrentDirectory(new File("H:\\tempPictures\\"));

            int rVal = jc.showOpenDialog(gui.this);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                f = jc.getSelectedFile();
                dir = jc.getCurrentDirectory().toString();
                filenamepath = dir + "\\" + jc.getSelectedFile().getName();
                original_file_size = jc.getSelectedFile().length();

                topImagePanel.setImageFile(jc.getSelectedFile());

                lblSizeImage.setText("Size of image: " + topImagePanel.iw + "x"
                        + topImagePanel.ih);
                lblSizeFile.setText("Size of file: "
                        + jc.getSelectedFile().length() + " b");
                lblNumSingVal
                        .setText("Number of sign val: " + topImagePanel.iw);
                jtfNumSingVal.setText(String.valueOf(topImagePanel.iw));

                if (f != null)
                    btnComp.setEnabled(true);
            }

            if (rVal == JFileChooser.CANCEL_OPTION) {
                System.out.println("You pressed cancel");
            }
        }

        if (e.getSource().equals(menuItemExit)) {
            System.exit(0);
        }

        if (e.getSource().equals(btnComp)) {

            Thread t = new Thread() {

                public void run() {
                    try {
                        compress_file_size = 0;

                        img = topImagePanel.getImage();

                        original_image orig_img = new original_image(img
                                .getHeight(), img.getWidth());

                        orig_img.compress(f, (int) Integer
                                .parseInt((jtfNumSingVal.getText())));

                        File f_comp = new File(filenamepath + ".svd");
                        compress_file_size = f_comp.length();

                        if (original_file_size > compress_file_size)
                            lblCompressCoeff.setText("Compress coeff: "
                                    + Math.round((double) original_file_size
                                    / (double) compress_file_size));
                        else
                            lblCompressCoeff.setText("Compress coeff: not");

                        JOptionPane.showMessageDialog(gui.this,
                                "Compress image save in: " + filenamepath
                                        + ".svd", "Image save!",
                                JOptionPane.INFORMATION_MESSAGE);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }

            };

            t.start();

        }

        if (e.getSource().equals(btnUnComp)) {

            Thread t = new Thread() {

                public void run() {
                    try {

                        class SVDFileFilter extends FileFilter {

                            public boolean accept(File file) {
                                if (file.isDirectory())
                                    return true;
                                return (file.getName().endsWith("svd"));
                            }

                            public String getDescription() {
                                return "Compress image (*.svd)";
                            }
                        }

                        jc_comp.setDialogTitle("Open compress image...");
                        jc_comp.setFileFilter(new SVDFileFilter());

                        if (new File("H:\\tempPictures\\").exists())
                            jc_comp.setCurrentDirectory(new File(
                                    "H:\\tempPictures\\"));

                        int rVal = jc_comp.showOpenDialog(gui.this);
                        if (rVal == JFileChooser.APPROVE_OPTION) {
                            compress_filename_path = jc_comp
                                    .getCurrentDirectory().toString()
                                    + "\\"
                                    + jc_comp.getSelectedFile().getName();
                            btnUnComp.setEnabled(true);
                        }
                        if (rVal == JFileChooser.CANCEL_OPTION) {
                            System.out.println("You pressed cancel");
                        }
                        if (compress_filename_path != null) {

                            compress_image comp_img = new compress_image(
                                    compress_filename_path);
                            uncompress_img = comp_img.uncompress();

                            botImagePanel.setImage(uncompress_img);

                            lblSizeFile1
                                    .setText("Size of compress file: "
                                            + jc_comp.getSelectedFile()
                                            .length() + " b");
                            lblSizeImage1.setText("Size of image: "
                                    + uncompress_img.getWidth() + "x"
                                    + uncompress_img.getHeight());
                            lblNumSingVal1.setText("Number of sign val: "
                                    + String.valueOf(comp_img.getN_s()));
                            System.out.println(compress_filename_path);
                            String uncomp_file_path = compress_filename_path
                                    .substring(0, compress_filename_path
                                            .indexOf("."))
                                    + "_uncomp.bmp";
                            JOptionPane.showMessageDialog(gui.this,
                                    "Uncompress image save in: "
                                            + uncomp_file_path, "Image save!",
                                    JOptionPane.INFORMATION_MESSAGE);

                        }
                    } catch (NumberFormatException ex) {
                        System.err
                                .println("Error in set of numbers singular values!");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }

            };

            t.start();

        }
    }

}
