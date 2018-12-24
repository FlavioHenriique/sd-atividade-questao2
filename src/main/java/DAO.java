import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DAO {

    private Object lock = new Object();
    private EntityManager em;
    private Chave chave;
    private int maximo;

    public DAO(int maximo) {
        em = Persistence.createEntityManagerFactory("SD").createEntityManager();
        insertChave();
        this.chave = chave();
        this.maximo = maximo;
    }


    public void salvar(Entidade entidade) {
        synchronized (lock) {
            em.getTransaction().begin();
            if (chave.getCodigo() > 0) {
                int incremento = (chave.getCodigo() * maximo) + entidade.getId();
                entidade.setId(incremento);
            }
            em.persist(entidade);
            em.getTransaction().commit();
            em.clear();
        }
    }

    private void insertChave() {
        synchronized (lock) {
            Chave atual;
            try {
                atual = chave();
            } catch (Exception e) {
                atual = null;
            }

            if (atual == null) {
                atual = new Chave();
                atual.setCodigo(0);
            } else {
                atual.setCodigo(atual.getCodigo() + 1);
            }
            em.getTransaction().begin();
            em.persist(atual);
            em.getTransaction().commit();
        }
    }


    public void atualizar(Entidade entidade) {
        synchronized (lock) {
            em.getTransaction().begin();
            em.merge(entidade);
            em.getTransaction().commit();
            em.clear();
        }
    }

    private Chave chave() {
        return (Chave) em.createQuery("select c from Chave c order by c.codigo ").getSingleResult();
    }

    public void cancelar() {
        int intervalo = (chave.getCodigo() * maximo);

        synchronized (lock) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Entidade where id between " + intervalo + " and " + intervalo + maximo)
                    .executeUpdate();
            em.getTransaction().commit();
        }
    }

}
