package mapd.solvers.proto.state;

import mapd.solvers.Interface.IAgent;
import mapd.solvers.Interface.IState;

public class DeliveryState implements IState{

    @Override
    public IState prepare(IAgent agent) {
        //another task is not necessary
        return this;
    }

    @Override
    public IState move(IAgent agent) {
        agent.elapseTime();
        //if arrive, transition to next state
        if(agent.isArrived()){
            return new TransactionState();
        }else{
            return this;
        }
    }
    
    @Override
    public IState transact(IAgent agent) {
        //if hasn't arrived, continue delivery
        return this;
    }

}