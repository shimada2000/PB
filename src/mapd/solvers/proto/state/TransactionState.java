package mapd.solvers.proto.state;

import mapd.solvers.Interface.IAgent;
import mapd.solvers.Interface.IState;

public class TransactionState implements IState {
    
    @Override
    public IState prepare(IAgent agent) {
        //another task is not necessary
        return this;
    }

    @Override
    public IState move(IAgent agent) {
        //stop there
        return this;
    }

    @Override
    public IState transact(IAgent agent) {
        agent.confirmDelivery();
        //if destination remains, continue delivery
        if(agent.updateGoal()){
            return new DeliveryState();
        }else{
            return new OffTaskState();
        }
    }
}