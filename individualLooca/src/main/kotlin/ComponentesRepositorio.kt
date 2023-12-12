import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate

class ComponentesRepositorio {
    lateinit var jdbcTemplate: JdbcTemplate;

    fun iniciar() {
        if (Conexao.local) jdbcTemplate = Conexao.jdbcTemplate!! else jdbcTemplate = Conexao.jdbcTemplateServer!!;
    }
    fun registrarComponentes(valor:Double, fkComponente:Int, maquina:Maquina){
        jdbcTemplate.update(
            "INSERT INTO Componentes_monitorados (valor, fkComponentesExistentes, fkMaquina, fkEmpMaqComp) VALUES (?, ?, ?, ?)",
            valor,
            fkComponente,
            maquina.idMaquina,
            maquina.fkEmpMaq
        );
    }
    /*
    fun verificarComponenteProcesso(idMaquina:Int):Boolean{
        val consulta = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) AS count FROM Componentes_monitorados WHERE fkMaquina = ? AND fkComponentesExistentes = ?;",
            arrayOf(idMaquina, 8),
            Int::class.java
        );
        return consulta == 1;
    }
    */
    fun limparProcessos(idMaquina: Int){
        jdbcTemplate.execute(
            "DELETE FROM Processo WHERE fkMaqProc = $idMaquina",
        );
    }
}