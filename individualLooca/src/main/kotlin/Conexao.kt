import com.github.britooo.looca.api.core.Looca
import org.apache.commons.dbcp2.BasicDataSource
import org.springframework.jdbc.core.JdbcTemplate

object Conexao {
    private val looca = Looca()
    private val so = looca.sistema.sistemaOperacional

    var user = if (so.contains("Win")) {
        "aluno";
    } else {
        "root";
    }
    val senha = if (so.contains("Win")) {
        "sptech";
    } else {
        "urubu100";
    }

    var userServer = "sa";
    var senhaServer = "centrix";

    val local:Boolean = false;

    var jdbcTemplate: JdbcTemplate? = null
        get() {
            if (field == null) {
                val dataSource = BasicDataSource();
                dataSource.url = "jdbc:mysql://localhost:3306/centrix?serverTimezone=UTC";
                dataSource.driverClassName = "com.mysql.cj.jdbc.Driver";
                dataSource.username = user;
                dataSource.password = senha;
                val novoJdbcTemplate = JdbcTemplate(dataSource);
                field = novoJdbcTemplate;
                jdbcTemplate!!.execute("use centrix;");
            }
            return field;
        }
    var jdbcTemplateServer: JdbcTemplate? = null
        get() {
            if (field == null) {
                val dataSourceServer = BasicDataSource();
                dataSourceServer.url = "jdbc:sqlserver://44.197.21.59;encrypt=false";
                dataSourceServer.driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                dataSourceServer.username = userServer;
                dataSourceServer.password = senhaServer;
                val novoJdbcTemplateServer = JdbcTemplate(dataSourceServer);
                field = novoJdbcTemplateServer;
                jdbcTemplateServer!!.execute("use centrix;");
            }
            return field;
        }
}