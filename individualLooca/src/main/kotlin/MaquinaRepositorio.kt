import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate

class MaquinaRepositorio {
    lateinit var jdbcTemplate: JdbcTemplate;

    fun iniciar() {
        if (Conexao.local) jdbcTemplate = Conexao.jdbcTemplate!! else jdbcTemplate = Conexao.jdbcTemplateServer!!;
    }
    fun verificarMaquina(id:String):Maquina{
        val maquina = jdbcTemplate.queryForObject(
            "SELECT idMaquina, Sistema_Operacional, Id_do_dispositivo, fkEmpMaq FROM Maquinas WHERE Id_do_dispositivo = ?;",
            arrayOf(id),
            BeanPropertyRowMapper(Maquina::class.java)
        );
        return maquina;
    }
    fun cadastrarMaquina(maquina:Maquina, usuario:Usuario):Maquina?{
        jdbcTemplate.update(
            "INSERT INTO Maquinas (Sistema_Operacional, Id_do_dispositivo, fkEmpMaq) VALUES (?, ?, ?)",
            maquina.Sistema_Operacional,
            maquina.Id_do_dispositivo,
            maquina.fkEmpMaq
        );
        jdbcTemplate.update(
            "INSERT INTO Notificacao (idDispositivo, Funcionario_Solicitante, fkEmpNot) VALUES (?, ?, ?)",
            maquina.Id_do_dispositivo,
            usuario.nome,
            maquina.fkEmpMaq
        );
        Thread.sleep(2 * 1000L);
        try {
            return verificarMaquina(maquina.Id_do_dispositivo);
        }
        catch (excecao: EmptyResultDataAccessException){
            return null;
        }
    }
}