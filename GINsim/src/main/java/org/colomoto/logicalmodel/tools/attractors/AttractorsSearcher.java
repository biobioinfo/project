package org.colomoto.logicalmodel.tools.attractors;

import java.util.List;

import org.colomoto.common.task.AbstractTask;
import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.tools.pushcount.StateTransitionGraph;

/**
 * Tools that computes all the Attractors in a given model. Attractors can be either
 * stable states, or strongly connected components that do not have outbond links.
 * @author heinrich
 *
 */
public class AttractorsSearcher extends AbstractTask<List<List<byte[]>>> {

	/**
	 * Describes which algorithm is used for the computation
	 *
	 */
	public static enum Algorithm {
		GARG_ET_AL,

		ZHENG_ET_AL ;

		public String toString(){
			switch(this)
			{
			case GARG_ET_AL:
				return "Garg et Al." ;
			case ZHENG_ET_AL :
				return "Zheng et Al." ;
			}
			return null;
		}
		/**
		 * Returns a string containing an URL where more information can be found about 
		 * this algorithm (for example, an URL to the paper that describe it).
		 * 
		 * @return
		 */
		public String getReference(){
			switch(this)
			{ 
			case GARG_ET_AL:
				return "http://link.springer.com/chapter/10.1007%2F978-3-540-71681-5_5" ;
			case ZHENG_ET_AL :
				return "http://journals.plos.org/plosone/article?id=10.1371/journal.pone.0060593" ;
			}
			return null;
		}
	}

	private final LogicalModel model;

	private final StateTransitionGraph trgraph ;

	private final Algorithm algo ;

	private final boolean synchronous ;


	/**
	 * Computes the attractors for the given model, with the given algorithm.
	 * @param model
	 * @param algo
	 * @param synchronous : whether to consider synchronous or asynchronous updates.
	 */
	public AttractorsSearcher(LogicalModel model, Algorithm algo, boolean synchronous){
		this.model = model ;
		this.trgraph = new StateTransitionGraph(model) ;
		this.algo = algo ;
		this.synchronous = synchronous ;
	}

	@Override
	public List<List<byte[]>> doGetResult() throws Exception {
		switch(algo)
		{
		case GARG_ET_AL :
			if(synchronous)
				return new GargAlgorithm(trgraph).computeSynchronousAttractors() ;
			else 
				return new GargAlgorithm(trgraph).computeAsynchronousAttractors() ;

		case ZHENG_ET_AL :
			if(synchronous)
				return new ZhengAlgorithm(trgraph).computeSynchronousAttractors() ;
			else 
				return new ZhengAlgorithm(trgraph).computeAsynchronousAttractors() ;
		}
		return null;
	}
}
