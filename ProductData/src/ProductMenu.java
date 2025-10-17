import javax.lang.model.type.NullType;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductMenu extends JFrame {
    public static void main(String[] args) {
        // buat object window
        ProductMenu menu = new ProductMenu();

        // atur ukuran window
        menu.setSize(700, 600);

        // letakkan window di tengah layar
        menu.setLocationRelativeTo(null);

        // isi window
        menu.setContentPane(menu.mainPanel);

        // ubah warna background
        menu.getContentPane().setBackground(Color.WHITE);

        // tampilkan window
        menu.setVisible(true);

        // agar program ikut berhenti saat window diclose
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    // index baris yang diklik
    private int selectedIndex = -1;
    // list untuk menampung semua produk
    //private ArrayList<Product> listProduct;
    private Database database;

    private JPanel mainPanel;
    private JTextField idField;
    private JTextField namaField;
    private JTextField signalPowerField;
    private JTable productTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox<String> kategoriComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JLabel idLabel;
    private JLabel namaLabel;
    private JLabel hargaLabel;
    private JLabel kategoriLabel;
    private JSlider frekuensiSlider;
    private JLabel frekuensiLabel;
    private JLabel frekuensiCounterLabel;

    // constructor
    public ProductMenu() {
        // inisialisasi listProduct
        //listProduct = new ArrayList<>();

        // buat objek database
        database = new Database();

        // isi listProduct
        populateList();

        // isi tabel produk
        productTable.setModel(setTable());

        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // atur isi combo box
        String[] kategoriData = {"????", "Publik", "Privat", "Rahasia"};
        kategoriComboBox.setModel(new DefaultComboBoxModel<>(kategoriData));

        // atur nilai minimum dan maksimum dari slider dan beberapa konfigurasi
        frekuensiSlider.setModel(new DefaultBoundedRangeModel(50000, 0, 0, 99999));

        // tampilkan nilai dari slider
        frekuensiSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double valueFrekuensi = frekuensiSlider.getValue() / 100.0;
                frekuensiCounterLabel.setText(String.format("%.2f Hz", valueFrekuensi));
            }
        });

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex == -1){
                    insertData();
                }
                else {
                    updateData();
                }
            }
        });
        // saat tombol delete ditekan
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: tambahkan konfirmasi sebelum menghapus data
                int confirm = JOptionPane.showConfirmDialog(null, "Yakin ingin membungkam data ini?", "Konfirmasi bungkam", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION){
                    deleteData();
                }
            }
        });
        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        // saat salah satu baris tabel ditekan
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = productTable.getSelectedRow();

                // simpan value textfield dan combo box
                String curID = productTable.getModel().getValueAt(selectedIndex, 1).toString();
                String curNama = productTable.getModel().getValueAt(selectedIndex, 2).toString();
                String curSignalPower = productTable.getModel().getValueAt(selectedIndex, 3).toString();
                curSignalPower =  curSignalPower.replace(",", ".");
                String curKategori = productTable.getModel().getValueAt(selectedIndex, 4).toString();
                String curFrekuensi = productTable.getModel().getValueAt(selectedIndex, 5).toString();
                curFrekuensi =  curFrekuensi.replace(",", ".");

                // ubah isi textfield dan combo box
                idField.setText(curID);
                namaField.setText(curNama);
                signalPowerField.setText(curSignalPower);
                kategoriComboBox.setSelectedItem(curKategori);
                double frekuensiDouble = Double.parseDouble(curFrekuensi);
                int curFrekuensiint = (int) (frekuensiDouble * 100);
                frekuensiSlider.setValue(curFrekuensiint);

                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");

                // tampilkan button delete
                deleteButton.setVisible(true);

            }
        });
    }

    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] cols = {"No", "ID Channel", "Nama", "Signal Power", "Kategori", "Frekuensi (Hz)"};

        // buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel tmp = new DefaultTableModel(null, cols);

        try {
            ResultSet resultSet = database.selectQuery("SELECT * FROM product");

            //isi tabel dengan hasil query
            int i = 0;
            while (resultSet.next()){
                Object[] row = new Object[6];
                row[0] = i + 1;
                row[1] = resultSet.getString("id");
                row[2] = resultSet.getString("nama");
                row[3] = resultSet.getString("signalPower");
                row[4] = resultSet.getString("kategori");
                row[5] = String.format("%.2f", resultSet.getDouble("frekuensi"));
                tmp.addRow(row);
                i++;
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }

//        // isi tabel dengan listProduct
//        for (int i = 0; i < listProduct.size(); i++){
//            Object[] row = { i + 1,
//                    listProduct.get(i).getId(),
//                    listProduct.get(i).getNama(),
//                    String.format("%d", listProduct.get(i).getSignalPower()),
//                    listProduct.get(i).getKategori(),
//                    String.format("%.2f", listProduct.get(i).getFrekuensi())
//            };
//            tmp.addRow(row);
//        }

        return tmp;
    }

    public void insertData() {
        try {
            // ambil value dari textfield dan combobox
            String id = idField.getText();
            String nama = namaField.getText();
            int signalPower = Integer.parseInt(signalPowerField.getText());
            String kategori = kategoriComboBox.getSelectedItem().toString();
            double frekuensi = frekuensiSlider.getValue() / 100.0;

            //tambahkan data ke dalam database
            if (!id.isEmpty() && !nama.isEmpty() && !signalPowerField.getText().isEmpty() && !kategori.equalsIgnoreCase("????") && frekuensi >= 0){
                String sqlQuery = "INSERT INTO product VALUES('" + id + "', '" + nama + "', '" + signalPower + "', '" + kategori + "', '" + frekuensi + "')";
                database.insertUpdateDeleteQuery(sqlQuery);

                // tambahkan data ke dalam list
                //listProduct.add(new Product(id, nama, signalPower, kategori, frekuensi));

                // update tabel
                productTable.setModel(setTable());

                // bersihkan form
                clearForm();

                // feedback
                System.out.println("Asyik bisa insert!");
                JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan");
            }
            else{
                JOptionPane.showMessageDialog(null, "Isi dulu semuanya bos!!", "Dongo", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex){
            JOptionPane.showMessageDialog(null, "Harus berupa angka!!", "Dongo", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateData() {
        try {
            // ambil data dari form
            String id = idField.getText();
            String nama = namaField.getText();
            int signalPower = Integer.parseInt(signalPowerField.getText());
            String kategori = kategoriComboBox.getSelectedItem().toString();
            double frekuensi = frekuensiSlider.getValue() / 100.0;

            //update data di dalam database
            if (!id.isEmpty() && !nama.isEmpty() && !signalPowerField.getText().isEmpty() && !kategori.equalsIgnoreCase("????") && frekuensi >= 0){
                String sqlQuery = "UPDATE product SET nama = '" + nama + "', signalPower = '" + signalPower + "', kategori = '" + kategori + "', frekuensi = '" + frekuensi + "' WHERE id = '" + id + "'";
                database.insertUpdateDeleteQuery(sqlQuery);

                // update tabel
                productTable.setModel(setTable());

                // bersihkan form
                clearForm();

                // feedback
                System.out.println("Asyik bisa update!");
                JOptionPane.showMessageDialog(null, "Data berhasil diupdate");
            }
            else{
                JOptionPane.showMessageDialog(null, "Isi dulu semuanya bos!!", "Dongo", JOptionPane.ERROR_MESSAGE);
            }

            // ubah data produk di list
            //listProduct.get(selectedIndex).setId(id);
            //listProduct.get(selectedIndex).setNama(nama);
            //listProduct.get(selectedIndex).setSignalPower(signalPower);
            //listProduct.get(selectedIndex).setKategori(kategori);
            //listProduct.get(selectedIndex).setFrekuensi(frekuensi);


        } catch (NumberFormatException ex){
            JOptionPane.showMessageDialog(null, "Harus berupa angka!!", "Dongo", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void deleteData() {
        //hapus data di database
        String id = idField.getText(); //ambil ID
        String sqlQuery = "DELETE FROM product WHERE id = '" + id + "'";
        database.insertUpdateDeleteQuery(sqlQuery);

        // hapus data dari list
        //listProduct.remove(selectedIndex);

        // update tabel
        productTable.setModel(setTable());

        // bersihkan form
        clearForm();

        // feedback
        System.out.println("Asyik bisa bungkam!");
        JOptionPane.showMessageDialog(null, "Data berhasil dibungkam");
    }

    public void clearForm() {
        // kosongkan semua texfield dan combo box
        idField.setText("");
        namaField.setText("");
        signalPowerField.setText("");
        kategoriComboBox.setSelectedIndex(0);
        frekuensiSlider.setValue(0);

        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;

    }

    // panggil prosedur ini untuk mengisi list produk
    private void populateList() {
//        listProduct.add(new Product("CH01", "KivotosRadio", 9, "Publik", 12.0));
//        listProduct.add(new Product("CH02", "Podcast Pak Tuah", 5, "Publik", 23.34));
//        listProduct.add(new Product("CH03", "AntTech2 Eye Sing", 10, "Rahasia", 11.100));
//        listProduct.add(new Product("CH04", "LCorp Announce", 10, "Privat", 99.69));
//        listProduct.add(new Product("CH05", "Terra Today", 7, "Publik", 123.9));
    }
}