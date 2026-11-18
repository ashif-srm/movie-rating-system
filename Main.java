import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final String URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:mysql://localhost:3306/movie_rating_db?useSSL=false&serverTimezone=UTC");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    private static Connection connect() throws SQLException {
        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException("Set the DB_PASSWORD environment variable before running.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        System.out.println("===== MOVIE RATING SYSTEM =====");
        while (true) {
            System.out.println("\n1. Register user\n2. Add movie\n3. View movies"
                    + "\n4. Rate/review movie\n5. Movie rankings\n6. View reviews"
                    + "\n7. Recommend movies\n0. Exit");
            int choice = readInt("Choose: ");
            try {
                switch (choice) {
                    case 1 -> registerUser();
                    case 2 -> addMovie();
                    case 3 -> viewMovies();
                    case 4 -> rateMovie();
                    case 5 -> rankings();
                    case 6 -> reviews();
                    case 7 -> recommend();
                    case 0 -> { System.out.println("Goodbye!"); return; }
                    default -> System.out.println("Invalid option.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a valid number."); }
        }
    }

    private static void registerUser() throws SQLException {
        System.out.print("Username: ");
        String name = sc.nextLine().trim();
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        if (name.isEmpty() || email.isEmpty()) { System.out.println("Fields cannot be empty."); return; }
        String sql = "INSERT INTO users(username,email) VALUES(?,?)";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, name); p.setString(2, email); p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) { if (r.next()) System.out.println("Registered. Your user ID: " + r.getInt(1)); }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("That email is already registered.");
        }
    }

    private static void addMovie() throws SQLException {
        System.out.print("Movie title: "); String title = sc.nextLine().trim();
        System.out.print("Genre: "); String genre = sc.nextLine().trim();
        int year = readInt("Release year: ");
        if (title.isEmpty() || genre.isEmpty() || year < 1888 || year > 2100) {
            System.out.println("Enter valid movie details."); return;
        }
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO movies(title,genre,release_year) VALUES(?,?,?)")) {
            p.setString(1, title); p.setString(2, genre); p.setInt(3, year);
            p.executeUpdate(); System.out.println("Movie added.");
        }
    }

    private static void viewMovies() throws SQLException {
        String sql = "SELECT m.movie_id,m.title,m.genre,m.release_year,COALESCE(AVG(r.rating),0) avg_rating,"
                + "COUNT(r.rating_id) votes FROM movies m LEFT JOIN ratings r ON m.movie_id=r.movie_id "
                + "GROUP BY m.movie_id,m.title,m.genre,m.release_year ORDER BY m.movie_id";
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            System.out.println("\nID | TITLE | GENRE | YEAR | AVERAGE | VOTES");
            while (r.next()) System.out.printf("%d | %s | %s | %d | %.2f/5 | %d%n",
                    r.getInt("movie_id"), r.getString("title"), r.getString("genre"),
                    r.getInt("release_year"), r.getDouble("avg_rating"), r.getInt("votes"));
        }
    }

    private static void rateMovie() throws SQLException {
        int uid = readInt("Your user ID: ");
        int mid = readInt("Movie ID: ");
        int rating = readInt("Rating (1-5): ");
        if (rating < 1 || rating > 5) { System.out.println("Rating must be 1 to 5."); return; }
        System.out.print("Review: "); String review = sc.nextLine().trim();
        String sql = "INSERT INTO ratings(user_id,movie_id,rating,review) VALUES(?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE rating=VALUES(rating),review=VALUES(review),created_at=CURRENT_TIMESTAMP";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, uid); p.setInt(2, mid); p.setInt(3, rating); p.setString(4, review);
            p.executeUpdate(); System.out.println("Rating and review saved.");
        }
    }

    private static void rankings() throws SQLException {
        String sql = "SELECT m.title,m.genre,AVG(r.rating) avg_rating,COUNT(*) votes "
                + "FROM movies m JOIN ratings r ON m.movie_id=r.movie_id "
                + "GROUP BY m.movie_id,m.title,m.genre ORDER BY avg_rating DESC,votes DESC";
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            int rank = 1;
            while (r.next()) System.out.printf("%d. %s (%s) - %.2f/5 (%d votes)%n",
                    rank++, r.getString("title"), r.getString("genre"), r.getDouble("avg_rating"), r.getInt("votes"));
            if (rank == 1) System.out.println("No ratings yet.");
        }
    }

    private static void reviews() throws SQLException {
        int mid = readInt("Movie ID: ");
        String sql = "SELECT u.username,r.rating,r.review,r.created_at FROM ratings r "
                + "JOIN users u ON r.user_id=u.user_id WHERE r.movie_id=? ORDER BY r.created_at DESC";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, mid);
            try (ResultSet r = p.executeQuery()) {
                boolean any = false;
                while (r.next()) {
                    any = true;
                    System.out.printf("%s - %d/5%nReview: %s%nDate: %s%n%n",
                            r.getString("username"), r.getInt("rating"), r.getString("review"), r.getTimestamp("created_at"));
                }
                if (!any) System.out.println("No reviews for this movie.");
            }
        }
    }

    private static void recommend() throws SQLException {
        int uid = readInt("Your user ID: ");
        String genreSql = "SELECT m.genre FROM ratings r JOIN movies m ON r.movie_id=m.movie_id "
                + "WHERE r.user_id=? GROUP BY m.genre ORDER BY AVG(r.rating) DESC,COUNT(*) DESC LIMIT 1";
        String genre = null;
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(genreSql)) {
            p.setInt(1, uid);
            try (ResultSet r = p.executeQuery()) { if (r.next()) genre = r.getString(1); }
        }
        if (genre == null) { System.out.println("Rate a movie first to receive recommendations."); return; }

        String sql = "SELECT m.movie_id,m.title,m.release_year,COALESCE(AVG(r.rating),0) avg_rating "
                + "FROM movies m LEFT JOIN ratings r ON m.movie_id=r.movie_id WHERE m.genre=? "
                + "AND NOT EXISTS(SELECT 1 FROM ratings mine WHERE mine.movie_id=m.movie_id AND mine.user_id=?) "
                + "GROUP BY m.movie_id,m.title,m.release_year ORDER BY avg_rating DESC";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, genre); p.setInt(2, uid);
            try (ResultSet r = p.executeQuery()) {
                System.out.println("Recommendations for genre: " + genre);
                boolean any = false;
                while (r.next()) {
                    any = true;
                    System.out.printf("%d. %s (%d) - %.2f/5%n", r.getInt("movie_id"),
                            r.getString("title"), r.getInt("release_year"), r.getDouble("avg_rating"));
                }
                if (!any) System.out.println("No unrated movies available in this genre.");
            }
        }
    }
}
