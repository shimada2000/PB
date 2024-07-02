package mapd.solvers.proto.state;

import mapd.solvers.Interface.IAgent;
import mapd.solvers.Interface.IState;

public class OffTaskState implements IState{

    @Override
    public IState prepare(IAgent agent) {
        if(agent.requestTask()){
            return new DeliveryState();
        }else{
            return this;
        }
    }

    @Override
    public IState move(IAgent agent) {
        //タスクを持っていなければどこにいようと自由
        return this;
    }

    @Override
    public IState transact(IAgent agent) {
        //タスクを持っていなければタスクを処理できない(呼び出されるが何もしない)
        return this;
    }
}