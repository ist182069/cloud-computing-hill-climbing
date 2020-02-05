
public class Request{

	private String _w;
	private String _h;
	private String _x0;
	private String _x1;
	private String _y0;
	private String _y1;
	private String _xS;
	private String _yS;
	private String _s;
	private String _i;
	private String _query;

    public Request(String w, String h, String x0, String x1, String y0, String y1, String xS, String yS, String s, String i, String query) {
    	_w = w;
    	_h = h;
    	_x0 = x0;
    	_x1 = x1;
    	_y0 = y0;
    	_y1 = y1;
    	_xS = xS;
    	_yS = yS;
    	_s = s;
    	_i = i;
    	_query = query;
    }

    public String getw() {
    	return this._w;
    }

    public String geth() {
    	return this._h;
    }

    public String getx0() {
    	return this._x0;
    }

    public String getx1() {
    	return this._x1;
    }

    public String gety0() {
    	return this._y0;
    }

    public String gety1() {
    	return this._y1;
    }

    public String getxS() {
    	return this._xS;
    }

    public String getyS() {
    	return this._yS;
    }

    public String gets() {
    	return this._s;
    }

    public String geti() {
    	return this._i;
    }

    public String getQuery() {
    	return this._query;
    }
    
	private double cost;
    public void setCost(Double valor){
    	cost = valor;
    } 
	
    public Double getCost(){
	return cost;	
    }

}
