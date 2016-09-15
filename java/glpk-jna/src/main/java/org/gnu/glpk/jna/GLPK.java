/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gnu.glpk.jna;

import com.sun.jna.*;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class GLPK implements GlpkConsts {

    static {
        Native.register(Platform.is64Bit() ? "glpk64" : "glpk32");
    }

    /**
     * LP/MIP problem object
     */
    public static class glp_prob extends PointerType {
    }

    /**
     * basis factorization control parameters
     */
    public static class glp_bfcp extends Structure implements Structure.ByReference {

        /**
         * (reserved)
         */
        public int msg_lev;
        /**
         * factorization type:
         */
        public int type;
        /**
         * luf.sv_size
         */
        public int lu_size;
        /**
         * luf.piv_tol
         */
        public double piv_tol;
        /**
         * luf.piv_lim
         */
        public int piv_lim;
        /**
         * luf.suhl
         */
        public int suhl;
        /**
         * luf.eps_tol
         */
        public double eps_tol;
        /**
         * luf.max_gro
         */
        public double max_gro;
        /**
         * fhv.hh_max
         */
        public int nfs_max;
        /**
         * fhv.upd_tol
         */
        public double upd_tol;
        /**
         * lpf.n_max
         */
        public int nrs_max;
        /**
         * lpf.v_size
         */
        public int rs_size;
        /**
         * (reserved)
         */
        public double[] foo_bar = new double[38];

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(
                    "msg_lev", "type", "lu_size", "piv_tol", "piv_lim", "suhl",
                    "eps_tol", "max_gro", "nfs_max", "upd_tol", "rfs_max",
                    "rs_size", "foo_bar");
        }
    }

    /**
     * simplex method control parameters
     */
    public static class glp_smcp extends Structure implements Structure.ByReference {

        /**
         * message level:
         */
        public int msg_lev;
        /**
         * simplex method option:
         */
        public int meth;
        /**
         * pricing technique:
         */
        public int pricing;
        /**
         * ratio test technique:
         */
        public int r_test;
        /**
         * spx.tol_bnd
         */
        public double tol_bnd;
        /**
         * spx.tol_dj
         */
        public double tol_dj;
        /**
         * spx.tol_piv
         */
        public double tol_piv;
        /**
         * spx.obj_ll
         */
        public double obj_ll;
        /**
         * spx.obj_ul
         */
        public double obj_ul;
        /**
         * spx.it_lim
         */
        public int it_lim;
        /**
         * spx.tm_lim (milliseconds)
         */
        public int tm_lim;
        /**
         * spx.out_frq
         */
        public int out_frq;
        /**
         * spx.out_dly (milliseconds)
         */
        public int out_dly;
        /**
         * enable/disable using LP presolver
         */
        public int presolve;
        /**
         * (reserved)
         */
        public double[] foo_bar = new double[36];

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(
                    "msg_lev", "meth", "pricing", "r_test", "tol_bnd", "tol_dj",
                    "tol_piv", "obj_ll", "obj_ul", "it_lim", "tm_lim", "out_frq",
                    "out_dly", "presolve", "foo_bar");
        }
    }

    /**
     * interior-point solver control parameters
     */
    public static class glp_iptcp extends Structure implements Structure.ByReference {

        /**
         * message level (see glp_smcp)
         */
        public int msg_lev;
        /**
         * ordering algorithm:
         */
        public int ord_alg;
        /**
         * (reserved)
         */
        public double[] foo_bar = new double[48];

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(
                    "msg_lev", "ord_alg", "foo_bar");
        }
    }

    /**
     * branch-and-bound tree
     */
    public static class glp_tree extends PointerType {
    }

    /**
     * integer optimizer control parameters
     */
    public static class glp_iocp extends Structure implements Structure.ByReference {

        /**
         * message level (see glp_smcp)
         */
        public int msg_lev;
        /**
         * branching technique:
         */
        public int br_tech;
        /**
         * backtracking technique:
         */
        public int bt_tech;
        /**
         * mip.tol_int
         */
        public double tol_int;
        /**
         * mip.tol_obj
         */
        public double tol_obj;
        /**
         * mip.tm_lim (milliseconds)
         */
        public int tm_lim;
        /**
         * mip.out_frq (milliseconds)
         */
        public int out_frq;
        /**
         * mip.out_dly (milliseconds)
         */
        public int out_dly;
        /**
         * mip.cb_func
         */
        public Pointer/*Function*/ cb_func; // void (*cb_func)(glp_tree *T, void *info);
        /**
         * mip.cb_info
         */
        public Pointer cb_info;
        /**
         * mip.cb_size
         */
        public int cb_size;
        /**
         * preprocessing technique:
         */
        public int pp_tech;
        /**
         * relative MIP gap tolerance
         */
        public double mip_gap;
        /**
         * MIR cuts (GLP_ON/GLP_OFF)
         */
        public int mir_cuts;
        /**
         * Gomory's cuts (GLP_ON/GLP_OFF)
         */
        public int gmi_cuts;
        /**
         * cover cuts (GLP_ON/GLP_OFF)
         */
        public int cov_cuts;
        /**
         * clique cuts (GLP_ON/GLP_OFF)
         */
        public int clq_cuts;
        /**
         * enable/disable using MIP presolver
         */
        public int presolve;
        /**
         * try to binarize integer variables
         */
        public int binarize;
        /**
         * feasibility pump heuristic
         */
        public int fp_heur;
        /**
         * use alien solver
         */
        public int alien;
        /**
         * (reserved)
         */
        public double[] foo_bar = new double[29];

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(
                    "msg_lev", "or_tech", "bt_tech", "tol_int", "tol_obj",
                    "tm_lim", "out_frq", "out_dly", "cb_func", "cb_info",
                    "cb_size", "pp_tech", "mip_gap", "mir_cuts", "gmi_cus",
                    "cov_cuts", "clq_cuts", "presolve", "binarize", "fp_heur",
                    "alien", "foo_bar");
        }
    }

    /**
     * additional row attributes
     */
    public static class glp_attr extends Structure implements Structure.ByReference {

        /**
         * subproblem level at which the row was added
         */
        public int level;
        /**
         * row origin flag:
         */
        public int origin;
        /**
         * row class descriptor:
         */
        public int klass;
        /**
         * (reserved)
         */
        double[] foo_bar = new double[7];

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(
                    "level", "origin", "klass", "foo_bar");
        }
    }

    /**
     * create problem object
     */
    public static native glp_prob glp_create_prob();

    /**
     * assign (change) problem name
     */
    public static native void glp_set_prob_name(glp_prob P, String name);

    /**
     * set (change) optimization direction flag
     */
    public static native void glp_set_obj_dir(glp_prob P, int dir);

    /**
     * add new rows to problem object
     */
    public static native int glp_add_rows(glp_prob P, int nrs);

    /**
     * add new columns to problem object
     */
    public static native int glp_add_cols(glp_prob P, int ncs);

    /**
     * assign (change) row name
     */
    public static native void glp_set_row_name(glp_prob P, int i, String name);

    /**
     * assign (change) column name
     */
    public static native void glp_set_col_name(glp_prob P, int j, String name);

    /**
     * set (change) row bounds
     */
    public static native void glp_set_row_bnds(glp_prob P, int i, int type, double lb, double ub);

    /**
     * set (change) column bounds
     */
    public static native void glp_set_col_bnds(glp_prob P, int j, int type, double lb, double ub);

    /**
     * set (change) obj. coefficient or constant term
     */
    public static native void glp_set_obj_coef(glp_prob P, int j, double coef);

    /**
     * set (replace) row of the constraint matrix
     */
    public static native void glp_set_mat_row(glp_prob P, int i, int len, int ind[], double val[]);

    /**
     * set (replace) column of the constraint matrix
     */
    public static native void glp_set_mat_col(glp_prob P, int j, int len, int ind[], double val[]);

    /**
     * load (replace) the whole constraint matrix
     */
    public static native void glp_load_matrix(glp_prob P, int ne, int ia[], int ja[], double ar[]);

    /**
     * check for duplicate elements in sparse matrix
     */
    public static native int glp_check_dup(int m, int n, int ne, int ia[], int ja[]);

    /**
     * sort elements of the constraint matrix
     */
    public static native void glp_sort_matrix(glp_prob P);

    /**
     * delete specified rows from problem object
     */
    public static native void glp_del_rows(glp_prob P, int nrs, int num[]);

    /**
     * delete specified columns from problem object
     */
    public static native void glp_del_cols(glp_prob P, int ncs, int num[]);

    /**
     * copy problem object content
     */
    public static native void glp_copy_prob(glp_prob dest, glp_prob prob, int names);

    /**
     * erase problem object content
     */
    public static native void glp_erase_prob(glp_prob P);

    /**
     * delete problem object
     */
    public static native void glp_delete_prob(glp_prob P);

    /**
     * retrieve problem name
     */
    public static native String glp_get_prob_name(glp_prob P);

    /**
     * retrieve objective function name
     */
    public static native String glp_get_obj_name(glp_prob P);

    /**
     * retrieve optimization direction flag
     */
    public static native int glp_get_obj_dir(glp_prob P);

    /**
     * retrieve number of rows
     */
    public static native int glp_get_num_rows(glp_prob P);

    /**
     * retrieve number of columns
     */
    public static native int glp_get_num_cols(glp_prob P);

    /**
     * retrieve row name
     */
    public static native String glp_get_row_name(glp_prob P, int i);

    /**
     * retrieve column name
     */
    public static native String glp_get_col_name(glp_prob P, int j);

    /**
     * retrieve row type
     */
    public static native int glp_get_row_type(glp_prob P, int i);

    /**
     * retrieve row lower bound
     */
    public static native double glp_get_row_lb(glp_prob P, int i);

    /**
     * retrieve row upper bound
     */
    public static native double glp_get_row_ub(glp_prob P, int i);

    /**
     * retrieve column type
     */
    public static native int glp_get_col_type(glp_prob P, int j);

    /**
     * retrieve column lower bound
     */
    public static native double glp_get_col_lb(glp_prob P, int j);

    /**
     * retrieve column upper bound
     */
    public static native double glp_get_col_ub(glp_prob P, int j);

    /**
     * retrieve obj. coefficient or constant term
     */
    public static native double glp_get_obj_coef(glp_prob P, int j);

    /**
     * retrieve number of constraint coefficients
     */
    public static native int glp_get_num_nz(glp_prob P);

    /**
     * retrieve row of the constraint matrix
     */
    public static native int glp_get_mat_row(glp_prob P, int i, int ind[], double val[]);

    /**
     * retrieve column of the constraint matrix
     */
    public static native int glp_get_mat_col(glp_prob P, int j, int ind[], double val[]);

    /**
     * create the name index
     */
    public static native void glp_create_index(glp_prob P);

    /**
     * find row by its name
     */
    public static native int glp_find_row(glp_prob P, String name);

    /**
     * find column by its name
     */
    public static native int glp_find_col(glp_prob P, String name);

    /**
     * delete the name index
     */
    public static native void glp_delete_index(glp_prob P);

    /**
     * set (change) row scale factor
     */
    public static native void glp_set_rii(glp_prob P, int i, double rii);

    /**
     * set (change) column scale factor
     */
    public static native void glp_set_sjj(glp_prob P, int j, double sjj);

    /**
     * retrieve row scale factor
     */
    public static native double glp_get_rii(glp_prob P, int i);

    /**
     * retrieve column scale factor
     */
    public static native double glp_get_sjj(glp_prob P, int j);

    /**
     * scale problem data
     */
    public static native void glp_scale_prob(glp_prob P, int flags);

    /**
     * unscale problem data
     */
    public static native void glp_unscale_prob(glp_prob P);

    /**
     * set (change) row status
     */
    public static native void glp_set_row_stat(glp_prob P, int i, int stat);

    /**
     * set (change) column status
     */
    public static native void glp_set_col_stat(glp_prob P, int j, int stat);

    /**
     * construct standard initial LP basis
     */
    public static native void glp_std_basis(glp_prob P);

    /**
     * construct advanced initial LP basis
     */
    public static native void glp_adv_basis(glp_prob P, int flags);

    /**
     * construct Bixby's initial LP basis
     */
    public static native void glp_cpx_basis(glp_prob P);

    /**
     * solve LP problem with the simplex method
     */
    public static native int glp_simplex(glp_prob P, glp_smcp parm);

    /**
     * solve LP problem in exact arithmetic
     */
    public static native int glp_exact(glp_prob P, glp_smcp parm);

    /**
     * initialize simplex method control parameters
     */
    public static native void glp_init_smcp(glp_smcp parm);

    /**
     * retrieve generic status of basic solution
     */
    public static native int glp_get_status(glp_prob P);

    /**
     * retrieve status of primal basic solution
     */
    public static native int glp_get_prim_stat(glp_prob P);

    /**
     * retrieve status of dual basic solution
     */
    public static native int glp_get_dual_stat(glp_prob P);

    /**
     * retrieve objective value (basic solution)
     */
    public static native double glp_get_obj_val(glp_prob P);

    /**
     * retrieve row status
     */
    public static native int glp_get_row_stat(glp_prob P, int i);

    /**
     * retrieve row primal value (basic solution)
     */
    public static native double glp_get_row_prim(glp_prob P, int i);

    /**
     * retrieve row dual value (basic solution)
     */
    public static native double glp_get_row_dual(glp_prob P, int i);

    /**
     * retrieve column status
     */
    public static native int glp_get_col_stat(glp_prob P, int j);

    /**
     * retrieve column primal value (basic solution)
     */
    public static native double glp_get_col_prim(glp_prob P, int j);

    /**
     * retrieve column dual value (basic solution)
     */
    public static native double glp_get_col_dual(glp_prob P, int j);

    /**
     * solve LP problem with the interior-point method
     */
    public static native int glp_interior(glp_prob P, glp_iptcp parm);

    /**
     * initialize interior-point solver control parameters
     */
    public static native void glp_init_iptcp(glp_iptcp parm);

    /**
     * retrieve status of interior-point solution
     */
    public static native int glp_ipt_status(glp_prob P);

    /**
     * retrieve objective value (interior point)
     */
    public static native double glp_ipt_obj_val(glp_prob P);

    /**
     * retrieve row primal value (interior point)
     */
    public static native double glp_ipt_row_prim(glp_prob P, int i);

    /**
     * retrieve row dual value (interior point)
     */
    public static native double glp_ipt_row_dual(glp_prob P, int i);

    /**
     * retrieve column primal value (interior point)
     */
    public static native double glp_ipt_col_prim(glp_prob P, int j);

    /**
     * retrieve column dual value (interior point)
     */
    public static native double glp_ipt_col_dual(glp_prob P, int j);

    /**
     * determine variable causing unboundedness
     */
    public static native int glp_get_unbnd_ray(glp_prob P);

    /**
     * set (change) column kind
     */
    public static native void glp_set_col_kind(glp_prob P, int j, int kind);

    /**
     * retrieve column kind
     */
    public static native int glp_get_col_kind(glp_prob P, int j);

    /**
     * retrieve number of integer columns
     */
    public static native int glp_get_num_int(glp_prob P);

    /**
     * retrieve number of binary columns
     */
    public static native int glp_get_num_bin(glp_prob P);

    /**
     * solve MIP problem with the branch-and-bound method
     */
    public static native int glp_intopt(glp_prob P, glp_iocp parm);

    /**
     * initialize integer optimizer control parameters
     */
    public static native void glp_init_iocp(glp_iocp parm);

    /**
     * retrieve status of MIP solution
     */
    public static native int glp_mip_status(glp_prob P);

    /**
     * retrieve objective value (MIP solution)
     */
    public static native double glp_mip_obj_val(glp_prob P);

    /**
     * retrieve row value (MIP solution)
     */
    public static native double glp_mip_row_val(glp_prob P, int i);

    /**
     * retrieve column value (MIP solution)
     */
    public static native double glp_mip_col_val(glp_prob P, int j);

    /**
     * write basic solution in printable format
     */
    public static native int glp_print_sol(glp_prob P, String fname);

    /**
     * read basic solution from text file
     */
    public static native int glp_read_sol(glp_prob P, String fname);

    /**
     * write basic solution to text file
     */
    public static native int glp_write_sol(glp_prob P, String fname);

    /**
     * print sensitivity analysis report
     */
    public static native int glp_print_ranges(glp_prob P, int len, int list[],
            int flags, String fname);

    /**
     * write interior-point solution in printable format
     */
    public static native int glp_print_ipt(glp_prob P, String fname);

    /**
     * read interior-point solution from text file
     */
    public static native int glp_read_ipt(glp_prob P, String fname);

    /**
     * write interior-point solution to text file
     */
    public static native int glp_write_ipt(glp_prob P, String fname);

    /**
     * write MIP solution in printable format
     */
    public static native int glp_print_mip(glp_prob P, String fname);

    /**
     * read MIP solution from text file
     */
    public static native int glp_read_mip(glp_prob P, String fname);

    /**
     * write MIP solution to text file
     */
    public static native int glp_write_mip(glp_prob P, String fname);

    /**
     * check if the basis factorization exists
     */
    public static native int glp_bf_exists(glp_prob P);

    /**
     * compute the basis factorization
     */
    public static native int glp_factorize(glp_prob P);

    /**
     * check if the basis factorization has been updated
     */
    public static native int glp_bf_updated(glp_prob P);

    /**
     * retrieve basis factorization control parameters
     */
    public static native void glp_get_bfcp(glp_prob P, glp_bfcp parm);

    /**
     * change basis factorization control parameters
     */
    public static native void glp_set_bfcp(glp_prob P, glp_bfcp parm);

    /**
     * retrieve the basis header information
     */
    public static native int glp_get_bhead(glp_prob P, int k);

    /**
     * retrieve row index in the basis header
     */
    public static native int glp_get_row_bind(glp_prob P, int i);

    /**
     * retrieve column index in the basis header
     */
    public static native int glp_get_col_bind(glp_prob P, int j);

    /**
     * perform forward transformation (solve system B*x = b)
     */
    public static native void glp_ftran(glp_prob P, double x[]);

    /**
     * perform backward transformation (solve system B'*x = b)
     */
    public static native void glp_btran(glp_prob P, double x[]);

    /**
     * "warm up" LP basis
     */
    public static native int glp_warm_up(glp_prob P);

    /**
     * compute row of the simplex tableau
     */
    public static native int glp_eval_tab_row(glp_prob P, int k, int ind[], double val[]);

    /**
     * compute column of the simplex tableau
     */
    public static native int glp_eval_tab_col(glp_prob P, int k, int ind[], double val[]);

    /**
     * transform explicitly specified row
     */
    public static native int glp_transform_row(glp_prob P, int len, int ind[], double val[]);

    /**
     * transform explicitly specified column
     */
    public static native int glp_transform_col(glp_prob P, int len, int ind[], double val[]);

    /**
     * perform primal ratio test
     */
    public static native int glp_prim_rtest(glp_prob P, int len, int ind[],
            double val[], int dir, double eps);

    /**
     * perform dual ratio test
     */
    public static native int glp_dual_rtest(glp_prob P, int len, int ind[],
            double val[], int dir, double eps);

    /**
     * analyze active bound of non-basic variable
     */
    public static native void glp_analyze_bound(glp_prob P, int k, DoubleByReference value1, IntByReference var1,
            DoubleByReference value2, IntByReference var2);

    /**
     * analyze objective coefficient at basic variable
     */
    public static native void glp_analyze_coef(glp_prob P, int k, DoubleByReference coef1, IntByReference var1,
            DoubleByReference value1, DoubleByReference coef2, IntByReference var2, DoubleByReference value2);

    /**
     * determine reason for calling the callback routine
     */
    public static native int glp_ios_reason(glp_tree T);

    /**
     * access the problem object
     */
    public static native glp_prob glp_ios_get_prob(glp_tree T);

    /**
     * determine size of the branch-and-bound tree
     */
    public static native void glp_ios_tree_size(glp_tree T, IntByReference a_cnt, IntByReference n_cnt,
            IntByReference t_cnt);

    /**
     * determine current active subproblem
     */
    public static native int glp_ios_curr_node(glp_tree T);

    /**
     * determine next active subproblem
     */
    public static native int glp_ios_next_node(glp_tree T, int p);

    /**
     * determine previous active subproblem
     */
    public static native int glp_ios_prev_node(glp_tree T, int p);

    /**
     * determine parent subproblem
     */
    public static native int glp_ios_up_node(glp_tree T, int p);

    /**
     * determine subproblem level
     */
    public static native int glp_ios_node_level(glp_tree T, int p);

    /**
     * determine subproblem local bound
     */
    public static native double glp_ios_node_bound(glp_tree T, int p);

    /**
     * find active subproblem with best local bound
     */
    public static native int glp_ios_best_node(glp_tree T);

    /**
     * compute relative MIP gap
     */
    public static native double glp_ios_mip_gap(glp_tree T);

    /**
     * access subproblem application-specific data
     */
    public static native Pointer glp_ios_node_data(glp_tree T, int p);

    /**
     * retrieve additional row attributes
     */
    public static native void glp_ios_row_attr(glp_tree T, int i, glp_attr attr);

    /**
     * determine current size of the cut pool
     */
    public static native int glp_ios_pool_size(glp_tree T);

    /**
     * add row (constraint) to the cut pool
     */
    public static native int glp_ios_add_row(glp_tree T,
            String name, int klass, int flags, int len, int ind[],
            double val[], int type, double rhs);

    /**
     * remove row (constraint) from the cut pool
     */
    public static native void glp_ios_del_row(glp_tree T, int i);

    /**
     * remove all rows (constraints) from the cut pool
     */
    public static native void glp_ios_clear_pool(glp_tree T);

    /**
     * check if can branch upon specified variable
     */
    public static native int glp_ios_can_branch(glp_tree T, int j);

    /**
     * choose variable to branch upon
     */
    public static native void glp_ios_branch_upon(glp_tree T, int j, int sel);

    /**
     * select subproblem to continue the search
     */
    public static native void glp_ios_select_node(glp_tree T, int p);

    /**
     * provide solution found by heuristic
     */
    public static native int glp_ios_heur_sol(glp_tree T, double x[]);

    /**
     * terminate the solution process
     */
    public static native void glp_ios_terminate(glp_tree T);

    /**
     * write problem data in MPS format
     */
    public static native int glp_write_mps(glp_prob P, int fmt, Pointer parn, String fname);

    /**
     * initialize GLPK environment
     */
    public static native int glp_init_env();

    /**
     * determine library version
     */
    public static native String glp_version();

    /**
     * free GLPK environment
     */
    public static native int glp_free_env();

    /**
     * write problem data in plain text format
     */
    public static native int _glp_lpx_print_prob(glp_prob P, String fname);

    /**
     * write LP problem solution in printable format
     */
    public static native int _glp_lpx_print_sol(glp_prob P, String fname);

    /**
     * Returns true if GLPK is reenterable on thios platform
     */
    public static boolean isReenterable() {
        //return Platform.isLinux() || !Platform.is64Bit();
        return true;
    }

    public static void main(String[] args) {
        int status;
        status = glp_free_env();
        assert status == 1;
        status = glp_init_env();
        assert status == 0;
        System.out.println("Version " + glp_version() + " " + (Platform.isWindows() ? "Win" : "Linux") + (Platform.is64Bit() ? "64" : "32"));
        status = glp_init_env();
        Thread thread = new Thread() {
            @Override
            public void run() {
                int s = glp_init_env();
                assert s == (isReenterable() ? 0 : 1);
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        assert status == 1;
        status = glp_free_env();
        assert status == 0;
        status = glp_free_env();
        assert status == 1;
    }
}
