import Conexao.jdbcTemplate
import com.github.britooo.looca.api.core.Looca
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import java.lang.Exception

object Cmd {
    private val looca = Looca();
    private val so = looca.sistema.sistemaOperacional;
    lateinit var jdbcTemplate: JdbcTemplate;
    fun iniciar() {
        if (Conexao.local) jdbcTemplate = Conexao.jdbcTemplate!! else jdbcTemplate = Conexao.jdbcTemplateServer!!;
    }
    private fun deletarPID(pid:Int) {
        if (so.contains("Win")){
            Runtime.getRuntime().exec("taskkill /F /IM $pid");
        } else {
            Runtime.getRuntime().exec("kill -9 $pid");
        }
    }
    fun buscarPid(idMaquina:Int){
        try {/*
            val pids:List<Int> = jdbcTemplate.query(
                "SELECT PID FROM Processo WHERE status = 0;",
                BeanPropertyRowMapper(Int::class.java)
            )*/
            val pids = jdbcTemplate.queryForList(
                "SELECT PID FROM Processo WHERE status = 0 AND fkMaqProc = $idMaquina;"
            )
            pids.forEach { row ->
                val pid = row["PID"] as? Int;
                if (pid != null) {
                    deletarPID(pid);
                }
            }
            jdbcTemplate.execute(
                "DELETE FROM Processo WHERE status = 0 AND fkMaqProc = $idMaquina;"
            )
        } catch (excecao:Exception){

        }
    }
}