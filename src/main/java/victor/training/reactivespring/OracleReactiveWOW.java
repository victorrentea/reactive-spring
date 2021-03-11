//package victor.training.reactivespring;
//
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//public class OracleReactiveWOW {
//   public static void main(String[] args) {
//
//      return Flux.using(
//
//          // Prepare a SQL statement.
//
//          () -> connection.prepareStatement("SELECT * FROM emp"),
//
//
//          // Execute the PreparedStatement.
//
//          preparedStatement ->
//
//              // Create a Mono which emits one ResultSet.
//
//              Mono.from(publishQuery(preparedStatement))
//
//                  // Flat map the ResultSet to a Flux which emits many Rows. Each row
//
//                  // is mapped to an Employee object.
//
//                  .flatMapMany(resultSet ->
//
//                      publishRows(resultSet, row -> mapRowToEmployee(row))),
//
//
//          // Close the PreparedStatement after emitting the last Employee object
//
//          prepareStatement -> {
//
//             try {
//
//                prepareStatement.close();
//
//             } catch (SQLException sqlException) {
//
//                throw new RuntimeException(sqlException);
//
//             }
//
//          });
//
//   }
//}
//}
