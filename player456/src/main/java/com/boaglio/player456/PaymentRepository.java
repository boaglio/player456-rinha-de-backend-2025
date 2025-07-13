package com.boaglio.player456;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final static String SQL_NEW_PAGAMENTO = "INSERT INTO pagamentos (amount, dt_transaction) VALUES (?, CURRENT_TIMESTAMP)";
    private final static String SQL_NEW_PAGAMENTO_FALLBACK = "INSERT INTO pagamentos_fallback (amount, dt_transaction) VALUES (?, CURRENT_TIMESTAMP)";
    private final static String SQL_NEW_RETRY_PAGAMENTO = "INSERT INTO pagamentos_retry (correlation_id,amount) VALUES (?,?)";

    private final static String SQL_PAGAMENTOS = "SELECT COALESCE(SUM(amount), 0) as total_amount, COUNT(*) as transaction_count FROM pagamentos WHERE dt_transaction BETWEEN ? AND ?";
    private final static String SQL_PAGAMENTOS_FALLBACK = "SELECT COALESCE(SUM(amount), 0) as total_amount, COUNT(*) as transaction_count FROM pagamentos_fallback WHERE dt_transaction BETWEEN ? AND ?";
    private static final String SQL_RETRY = "SELECT id, correlation_id, amount FROM pagamentos_retry ORDER BY id LIMIT 10";
    private static final String SQL_COUNT_RETRY = "SELECT COUNT(*) FROM pagamentos_retry";

    private final static String SQL_DELETE_RETRY_PAGAMENTO = "DELETE FROM pagamentos_retry WHERE ID=?";
    private final static String SQL_TRUNCATE_RETRY_PAGAMENTO = "TRUNCATE TABLE pagamentos_retry";

    private final static String SQL_ALL_PAGAMENTOS = "SELECT id, amount, dt_transaction FROM pagamentos";
    private final static String SQL_ALL_PAGAMENTOS_FALLBACK = "SELECT id, amount, dt_transaction FROM pagamentos_fallback";
    private final static String SQL_ALL_PAGAMENTOS_RETRY = "SELECT id, correlation_id, amount FROM pagamentos_retry";

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addDefaultPayment(String amount) {
        try {
            double parsedAmount = Double.parseDouble(amount);
            jdbcTemplate.update(SQL_NEW_PAGAMENTO, parsedAmount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + amount, e);
        }
    }

    public void addFallbackPayment(String amount) {
        try {
            double parsedAmount = Double.parseDouble(amount);
            jdbcTemplate.update(SQL_NEW_PAGAMENTO_FALLBACK, parsedAmount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + amount, e);
        }
    }

    public void truncateRetryPayment() {
        jdbcTemplate.update(SQL_TRUNCATE_RETRY_PAGAMENTO);
    }

    public void addRetryPayment(String correlationId,String amount) {
        jdbcTemplate.update(SQL_NEW_RETRY_PAGAMENTO, correlationId.trim(),amount.trim());
    }

    @Transactional
    public List<PaymentRetry> getPaymentForRetry() {

        List<PaymentRetry> paymentRetry;
        try {

            paymentRetry = jdbcTemplate.query(SQL_RETRY, (rs, rowNum) ->
                    new PaymentRetry(
                            rs.getLong("id"),
                            rs.getString("correlation_id").trim(),
                            rs.getString("amount").trim()
                    )
            );

            paymentRetry.forEach( p -> jdbcTemplate.update(SQL_DELETE_RETRY_PAGAMENTO, p.id()));

        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null; // Return null if no records are found
        }
        return paymentRetry;
    }

    public PaymentSummary getSummary(LocalDateTime from, LocalDateTime to) {
        try {
            if (from.isAfter(to)) {
                throw new IllegalArgumentException("'from' date must be before 'to' date");
            }

            // pagamentos
            Object[] params = new Object[]{from, to};
            int[] types = new int[]{Types.TIMESTAMP, Types.TIMESTAMP};
            Object[] resultPagamentos = jdbcTemplate.queryForObject(SQL_PAGAMENTOS, params, types, (rs, rowNum) ->
                    new Object[]{rs.getDouble("total_amount"), rs.getLong("transaction_count")}
            );

            // pagamentos_fallback
            Object[] resultFallback = jdbcTemplate.queryForObject(SQL_PAGAMENTOS_FALLBACK, params, types, (rs, rowNum) ->
                    new Object[]{rs.getDouble("total_amount"), rs.getLong("transaction_count")}
            );

            // Extract results
            double totalAmount = resultPagamentos[0] != null ? (Double) resultPagamentos[0] : 0.0;
            long totalRequests = resultPagamentos[1] != null ? (Long) resultPagamentos[1] : 0L;
            double totalAmountFallback = resultFallback[0] != null ? (Double) resultFallback[0] : 0.0;
            long totalRequestsFallback = resultFallback[1] != null ? (Long) resultFallback[1] : 0L;

            return new PaymentSummary(totalRequests, totalAmount, totalRequestsFallback, totalAmountFallback);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO 8601 format (e.g., '2020-07-10T12:34:56.000Z')", e);
        }
    }

    public List<PaymentRecord> getAllPagamentos() {
        return jdbcTemplate.query(SQL_ALL_PAGAMENTOS, (rs, rowNum) ->
                new PaymentRecord(
                        rs.getLong("id"),
                        rs.getDouble("amount"),
                        rs.getObject("dt_transaction", java.time.OffsetDateTime.class).atZoneSameInstant(java.time.ZoneId.of("America/Sao_Paulo"))
                )
        );
    }

    public List<PaymentRecord> getAllPagamentosFallback() {
        return jdbcTemplate.query(SQL_ALL_PAGAMENTOS_FALLBACK, (rs, rowNum) ->
                new PaymentRecord(
                        rs.getLong("id"),
                        rs.getDouble("amount"),
                        rs.getObject("dt_transaction", java.time.OffsetDateTime.class).atZoneSameInstant(java.time.ZoneId.of("America/Sao_Paulo"))
                )
        );
    }

    public Long getCountRetryPagamentos() {
        return jdbcTemplate.queryForObject(SQL_COUNT_RETRY, (rs, rowNum) ->
                rs.getLong(1)
        );
    }

    public List<PaymentRetry> getAllPagamentosRetry() {
        return jdbcTemplate.query(SQL_ALL_PAGAMENTOS_RETRY, (rs, rowNum) ->
                new PaymentRetry(
                        rs.getLong("id"),
                        rs.getString("correlation_id"),
                        rs.getString("amount")
                )
        );
    }
}