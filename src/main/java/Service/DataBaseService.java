package Service;

import Model.Evento;
import Model.UF;
import Provider.DataBaseProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;

public class DataBaseService {
    private DataBaseProvider dataBaseProvider;
    private JdbcTemplate jdbcTemplate;

    public DataBaseService() {
        this.dataBaseProvider = new DataBaseProvider();
        this.jdbcTemplate = dataBaseProvider.getConnection();
    }

    public List<UF> getUfs() {
        String sql = "WITH EventoMaisProximo AS ("
                + "SELECT e.idEvento, e.Nome, e.DataInicio, e.DataFim, "
                + "ABS(DATEDIFF(e.DataInicio, CURRENT_DATE)) AS diff_days "
                + "FROM Evento e "
                + "ORDER BY diff_days ASC LIMIT 1), "
                + "TotalCrimesAno AS ("
                + "SELECT c.fkEstado, COUNT(*) AS total_crimes_year "
                + "FROM Crime c "
                + "JOIN EventoMaisProximo ep ON YEAR(c.Data) = YEAR(ep.DataInicio) "
                + "WHERE YEAR(c.Data) = YEAR(ep.DataInicio) "
                + "GROUP BY c.fkEstado), "
                + "CrimesDuranteEvento AS ("
                + "SELECT c.fkEstado, COUNT(*) AS crimes_durante_evento "
                + "FROM Crime c "
                + "JOIN EventoMaisProximo ep ON YEAR(c.Data) = YEAR(ep.DataInicio) "
                + "AND c.Data BETWEEN ep.DataInicio AND ep.DataFim "
                + "GROUP BY c.fkEstado), "
                + "TotalViagensAno AS ("
                + "SELECT a.fkEstado, COUNT(*) AS total_viagens_year "
                + "FROM Viagem v "
                + "JOIN Aeroporto a ON v.fkAeroportoDestino = a.idAeroporto "
                + "JOIN EventoMaisProximo ep ON YEAR(v.dtViagem) = YEAR(ep.DataInicio) "
                + "WHERE YEAR(v.dtViagem) = YEAR(ep.DataInicio) "
                + "GROUP BY a.fkEstado), "
                + "ViagensDuranteEvento AS ("
                + "SELECT a.fkEstado, COUNT(*) AS viagens_durante_evento "
                + "FROM Viagem v "
                + "JOIN Aeroporto a ON v.fkAeroportoDestino = a.idAeroporto "
                + "JOIN EventoMaisProximo ep ON YEAR(v.dtViagem) = YEAR(ep.DataInicio) "
                + "AND v.dtViagem BETWEEN ep.DataInicio AND ep.DataFim "
                + "GROUP BY a.fkEstado), "
                + "Scores AS ("
                + "SELECT uf.Nome AS nome_estado, "
                + "IFNULL(cd.crimes_durante_evento, 0) / IFNULL(tc.total_crimes_year, 1) AS safety_score, "
                + "IFNULL(vde.viagens_durante_evento, 0) / IFNULL(tv.total_viagens_year, 1) AS visit_score "
                + "FROM UF uf "
                + "LEFT JOIN TotalCrimesAno tc ON tc.fkEstado = uf.CodigoIBGE "
                + "LEFT JOIN CrimesDuranteEvento cd ON cd.fkEstado = uf.CodigoIBGE "
                + "LEFT JOIN TotalViagensAno tv ON tv.fkEstado = uf.CodigoIBGE "
                + "LEFT JOIN ViagensDuranteEvento vde ON vde.fkEstado = uf.CodigoIBGE), "
                + "Ranked AS ("
                + "SELECT nome_estado, safety_score, visit_score, "
                + "ROW_NUMBER() OVER (ORDER BY safety_score ASC, visit_score DESC) AS 'rank' "
                + "FROM Scores) "
                + "SELECT nome_estado FROM Ranked WHERE 'rank' <= 5" +
                " LIMIT 5;";

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                UF uf = new UF(rs.getString("nome_estado"));
                return uf;
            });
        } catch (DataAccessException e) {
            System.out.println("Erro ao executar a consulta para UFs: " + e);
            return Collections.emptyList();
        }
    }

    public List<Evento> getProximoEvento() {
        String sql = "SELECT Nome, ABS(DATEDIFF(e.DataInicio, CURRENT_DATE)) AS diff_days FROM Evento e ORDER BY diff_days ASC LIMIT 1;";

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Evento evento = new Evento(rs.getString("Nome"));
                return evento;
            });
        } catch (DataAccessException e) {
            System.out.println("Erro ao executar a consulta para UFs: " + e);
            return Collections.emptyList();
        }
    }
}