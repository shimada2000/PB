package mapd.solvers.Interface;

public interface IState {
    //エージェントの処理はこの順に呼び出される
    public IState prepare(IAgent agent);
    public IState move(IAgent agent);
    public IState transact(IAgent agent);
}
