import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class InventoryService {

    public void initDataDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    public List<Product> loadProducts(Path csvPath) throws IOException {
        List<Product> products = new ArrayList<>();
        if (Files.exists(csvPath)) {
            List<String> lines = Files.readAllLines(csvPath);
            for (int i = 1; i < lines.size(); i++) {
                products.add(Product.fromCsvLine(lines.get(i)));
            }
        }
        return products;
    }

    public void saveProducts(List<Product> products, Path csvPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(csvPath))) {
            writer.println("id,name,category,price,quantity");
            for (Product p : products) {
                writer.println(p.toCsvLine());
            }
        }
    }

    public List<Product> searchProducts(List<Product> products, String keyword) {
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void sortProducts(List<Product> products, String criteria) {
        if (criteria.equalsIgnoreCase("price")) {
            products.sort(Comparator.comparingDouble(Product::getPrice));
        } else if (criteria.equalsIgnoreCase("quantity")) {
            products.sort(Comparator.comparingInt(Product::getQuantity));
        }
    }

    public List<Product> filterByPrice(List<Product> products, double min, double max) {
        return products.stream()
                .filter(p -> p.getPrice() >= min && p.getPrice() <= max)
                .collect(Collectors.toList());
    }

    public void runMenu(List<Product> products) throws IOException {
        Scanner sc = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n=== INVENTORY MANAGER ===");
            System.out.println("1. Lihat semua");
            System.out.println("2. Tambah produk");
            System.out.println("3. Update stok");
            System.out.println("4. Hapus produk");
            System.out.println("5. Cari produk");
            System.out.println("6. Sort produk");
            System.out.println("7. Filter harga");
            System.out.println("8. Simpan & Keluar");
            System.out.print("Pilih: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    viewAll(products);
                    break;
                case "2":
                    addProduct(products, sc);
                    break;
                case "3":
                    updateQuantity(products, sc);
                    break;
                case "4":
                    deleteProduct(products, sc);
                    break;
                case "5":
                    System.out.print("Kata kunci: ");
                    String keyword = sc.nextLine();
                    List<Product> result = searchProducts(products, keyword);
                    viewAll(result);
                    break;
                case "6":
                    System.out.print("Sort by (price/quantity): ");
                    String criteria = sc.nextLine();
                    sortProducts(products, criteria);
                    viewAll(products);
                    break;
                case "7":
                    System.out.print("Harga minimum: ");
                    double min = Double.parseDouble(sc.nextLine());
                    System.out.print("Harga maksimum: ");
                    double max = Double.parseDouble(sc.nextLine());
                    List<Product> filtered = filterByPrice(products, min, max);
                    viewAll(filtered);
                    break;
                case "8":
                    running = false;
                    break;
                default:
                    System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private void viewAll(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("Belum ada produk.");
            return;
        }
        System.out.println("ID | Nama | Kategori | Harga | Stok");
        for (Product p : products) {
            System.out.println(p.getId() + " | " + p.getName() + " | " +
                    p.getCategory() + " | " + p.getPrice() + " | " + p.getQuantity());
        }
    }

    private void addProduct(List<Product> products, Scanner sc) {
        System.out.print("ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("Nama: ");
        String name = sc.nextLine();
        System.out.print("Kategori: ");
        String category = sc.nextLine();
        System.out.print("Harga: ");
        double price = Double.parseDouble(sc.nextLine());
        System.out.print("Kuantitas: ");
        int quantity = Integer.parseInt(sc.nextLine());

        products.add(new Product(id, name, category, price, quantity));
        System.out.println("Produk berhasil ditambahkan.");
    }

    private void updateQuantity(List<Product> products, Scanner sc) {
        System.out.print("ID produk yang ingin diupdate: ");
        int id = Integer.parseInt(sc.nextLine());
        for (Product p : products) {
            if (p.getId() == id) {
                System.out.print("Kuantitas baru: ");
                int quantity = Integer.parseInt(sc.nextLine());
                p.setQuantity(quantity);
                System.out.println("Stok diperbarui.");
                return;
            }
        }
        System.out.println("Produk tidak ditemukan.");
    }

    private void deleteProduct(List<Product> products, Scanner sc) {
        System.out.print("ID produk yang ingin dihapus: ");
        int id = Integer.parseInt(sc.nextLine());
        boolean removed = products.removeIf(p -> p.getId() == id);
        if (removed) {
            System.out.println("Produk berhasil dihapus.");
        } else {
            System.out.println("Produk tidak ditemukan.");
        }
    }
}
