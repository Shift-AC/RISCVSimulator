/*
 ****************************************************************************
 *
 *                   "DHRYSTONE" Benchmark Program
 *                   -----------------------------
 *                                                                            
 *  Version:    C, Version 2.1
 *                                                                            
 *  File:       dhry_1.c (part 2 of 3)
 *
 *  Date:       May 17, 1988
 *
 *  Author:     Reinhold P. Weicker
 *
 ****************************************************************************
 */

#include "dhry.h"

#define TIMES
/* Global Variables: */

Rec_Pointer     Ptr_Glob = (void*)1,
                Next_Ptr_Glob = (void*)1;
int             Int_Glob = 1;
Boolean         Bool_Glob = 1;
char            Ch_1_Glob = 1,
                Ch_2_Glob = 1;
int             Arr_1_Glob [50] = { 1 };
int             Arr_2_Glob [50] [50] = {{ 1 }};

//extern char     *malloc ();
Enumeration     Func_1 ();
  /* forward declaration necessary since Enumeration may not simply be int */

#ifndef REG
        Boolean Reg = false;
#define REG
        /* REG becomes defined as empty */
        /* i.e. no register variables   */
#else
        Boolean Reg = true;
#endif

/* variables for time measurement: */

#ifdef TIMES
//extern  int     times ();
                /* see library function "times" */
#define Too_Small_Time (2*HZ)
                /* Measurements should last at least about 2 seconds */
#endif

#define Mic_Secs_Per_Second ((float)1000000)

long            Begin_Time = 1,
                End_Time = 1,
                User_Time = 1;
float           Microseconds = 1.0f,
                Dhrystones_Per_Second = 1.0f;

/* end of variables for time measurement */

void setGlobal()
{
    Ptr_Glob = 0;
    Next_Ptr_Glob = 0;
    Int_Glob = 0;
    Bool_Glob = 0;
    Ch_1_Glob = 0;
    Ch_2_Glob = 0;
    Arr_1_Glob[0] = 0;
    Arr_2_Glob[0][0] = 0;
    Begin_Time = 0;
    End_Time = 0;
    User_Time = 0;
    Microseconds = 0.0f;
    Dhrystones_Per_Second = 0.0f;
}

main ()
/*****/

  /* main program, corresponds to procedures        */
  /* Main and Proc_0 in the Ada version             */
{
        One_Fifty       Int_1_Loc;
  REG   One_Fifty       Int_2_Loc;
        One_Fifty       Int_3_Loc;
  REG   char            Ch_Index;
        Enumeration     Enum_Loc;
        Str_30          Str_1_Loc;
        Str_30          Str_2_Loc;
  REG   int             Run_Index;
  REG   int             Number_Of_Runs;

#ifdef TIMES
  struct tms      time_info;
#endif
  setGlobal();

  /* Initializations */

  Rec_Type p;
  Rec_Type np;

  Next_Ptr_Glob = &np;
  Ptr_Glob = &p;

  Ptr_Glob->Ptr_Comp                    = Next_Ptr_Glob;
  Ptr_Glob->Discr                       = Ident_1;
  Ptr_Glob->variant.var_1.Enum_Comp     = Ident_3;
  Ptr_Glob->variant.var_1.Int_Comp      = 40;
  strcpy (Ptr_Glob->variant.var_1.Str_Comp, 
          "DHRYSTONE PROGRAM, SOME STRING");
  strcpy (Str_1_Loc, "DHRYSTONE PROGRAM, 1'ST STRING");

  Arr_2_Glob [8][7] = 10;
        /* Was missing in published program. Without this statement,    */
        /* Arr_2_Glob [8][7] would have an undefined value.             */
        /* Warning: With 16-Bit processors and Number_Of_Runs > 32000,  */
        /* overflow may occur for this array element.                   */

  printnewline();
  printstring("Dhrystone Benchmark, Version 2.1 (Language: C)\n");
  printnewline();
  if (Reg)
  {
    printstring("Program compiled with 'register' attribute\n");
    printnewline();
  }
  else
  {
    printstring("Program compiled without 'register' attribute\n");
    printnewline();
  }
  printstring("Please give the number of runs through the benchmark: \n");
  {
    int n = 10;
    readnum(&n);
    Number_Of_Runs = n;
  }
  printnewline();

  printstring("Execution starts, ");
  printnum(Number_Of_Runs);
  printstring(" runs through Dhrystone\n");

  /***************/
  /* Start timer */
  /***************/
 
#ifdef TIMES
  times (&time_info);
  Begin_Time = (long) time_info.tms_utime;
#endif
  for (Run_Index = 1; Run_Index <= Number_Of_Runs; ++Run_Index)
  {

    Proc_5();
    Proc_4();
      /* Ch_1_Glob == 'A', Ch_2_Glob == 'B', Bool_Glob == true */
    Int_1_Loc = 2;
    Int_2_Loc = 3;
    strcpy (Str_2_Loc, "DHRYSTONE PROGRAM, 2'ND STRING");
    Enum_Loc = Ident_2;
    Bool_Glob = ! Func_2 (Str_1_Loc, Str_2_Loc);
      /* Bool_Glob == 1 */
    while (Int_1_Loc < Int_2_Loc)  /* loop body executed once */
    {
      Int_3_Loc = 5 * Int_1_Loc - Int_2_Loc;
        /* Int_3_Loc == 7 */
      Proc_7 (Int_1_Loc, Int_2_Loc, &Int_3_Loc);
        /* Int_3_Loc == 7 */
      Int_1_Loc += 1;
    } /* while */
//printstring("3 ");printnum(Int_2_Loc);printnewline();
      /* Int_1_Loc == 3, Int_2_Loc == 3, Int_3_Loc == 7 */
    Proc_8 (Arr_1_Glob, Arr_2_Glob, Int_1_Loc, Int_3_Loc);
      /* Int_Glob == 5 */
    Proc_1 (Ptr_Glob);
    for (Ch_Index = 'A'; Ch_Index <= Ch_2_Glob; ++Ch_Index)
                             /* loop body executed twice */
    {
      if (Enum_Loc == Func_1 (Ch_Index, 'C'))
          /* then, not executed */
        {
        Proc_6 (Ident_1, &Enum_Loc);
        strcpy (Str_2_Loc, "DHRYSTONE PROGRAM, 3'RD STRING");
        Int_2_Loc = Run_Index;
        Int_Glob = Run_Index;
        }
    }
//printstring("3 ");printnum(Int_2_Loc);printnewline();
      /* Int_1_Loc == 3, Int_2_Loc == 3, Int_3_Loc == 7 */
    Int_2_Loc = Int_2_Loc * Int_1_Loc;
    Int_1_Loc = Int_2_Loc / Int_3_Loc;
    Int_2_Loc = 7 * (Int_2_Loc - Int_3_Loc) - Int_1_Loc;
/*printnum(Int_2_Loc);*/
      /* Int_1_Loc == 1, Int_2_Loc == 13, Int_3_Loc == 7 */
    Proc_2 (&Int_1_Loc);
      /* Int_1_Loc == 5 */

  } /* loop "for Run_Index" */

  /**************/
  /* Stop timer */
  /**************/
#ifdef TIMES
  times (&time_info);
  End_Time = (long) time_info.tms_utime;
#endif

  printstring("Execution ends\n");
  printnewline();
  printstring("Final values of the variables used in the benchmark:\n");
  printnewline();
  
  printstring("Int_Glob:            ");
  printnum(Int_Glob);
  printnewline();
  printstring("        should be:   5\n");
  
  printstring("Bool_Glob:           ");
  printnum(Bool_Glob);
  printnewline();
  printstring("        should be:   1\n");
  
  printstring("Ch_1_Glob:           ");
  printnum(Ch_1_Glob);
  printnewline();
  printstring("        should be:   'A'\n");
  
  printstring("Ch_2_Glob:           ");
  printnum(Ch_2_Glob);
  printnewline();
  printstring("        should be:   'B'\n");
  
  printstring("Arr_1_Glob[8]:       ");
  printnum(Arr_1_Glob[8]);
  printnewline();
  printstring("        should be:   7\n");

  printstring("Arr_2_Glob[8][7]:    ");
  printnum(Arr_2_Glob[8][7]);
  printnewline();
  printstring("        should be:   Number_Of_Runs + 10\n");

  printstring("Ptr_Glob->\n");

  printstring("  Ptr_Comp:          ");
  printnum((int) Ptr_Glob->Ptr_Comp);
  printnewline();
  printstring("        should be:   (implementation-dependent)\n");

  printstring("  Discr:             ");
  printnum(Ptr_Glob->Discr);
  printnewline();
  printstring("        should be:   0\n");

  printstring("  Enum_Comp:         ");
  printnum(Ptr_Glob->variant.var_1.Enum_Comp);
  printnewline();
  printstring("        should be:   2\n");

  printstring("  Int_Comp:          ");
  printnum(Ptr_Glob->variant.var_1.Int_Comp);
  printnewline();
  printstring("        should be:   17\n");

  printstring("  Str_Comp:          ");
  printstring(Ptr_Glob->variant.var_1.Str_Comp);
  printnewline();
  printstring("        should be:   DHRYSTONE PROGRAM, SOME STRING\n");

  printstring("Next_Ptr_Glob->\n");

  printstring("  Ptr_Comp:          ");
  printnum((int) Next_Ptr_Glob->Ptr_Comp);
  printnewline();
  printstring("        should be:   (implementation-dependent), same as above\n");

  printstring("  Discr:             ");
  printnum(Next_Ptr_Glob->Discr);
  printnewline();
  printstring("        should be:   0\n");

  printstring("  Enum_Comp:         ");
  printnum(Next_Ptr_Glob->variant.var_1.Enum_Comp);
  printnewline();
  printstring("        should be:   1\n");

  printstring("  Int_Comp:          ");
  printnum(Next_Ptr_Glob->variant.var_1.Int_Comp);
  printnewline();
  printstring("        should be:   18\n");

  printstring("  Str_Comp:          ");
  printstring(Next_Ptr_Glob->variant.var_1.Str_Comp);
  printnewline();
  printstring("        should be:   DHRYSTONE PROGRAM, SOME STRING\n");

  printstring("Int_1_Loc:           ");
  printnum(Int_1_Loc);
  printnewline();
  printstring("        should be:   5\n");

  printstring("Int_2_Loc:           ");
  printnum(Int_2_Loc);
  printnewline();
  printstring("        should be:   13\n");

  printstring("Int_3_Loc:           ");
  printnum(Int_3_Loc);
  printnewline();
  printstring("        should be:   7\n");

  printstring("Enum_Loc:            ");
  printnum(Enum_Loc);
  printnewline();
  printstring("        should be:   1\n");

  printstring("Str_1_Loc:           ");
  printstring(Str_1_Loc);
  printnewline();
  printstring("        should be:   DHRYSTONE PROGRAM, 1'ST STRING\n");

  printstring("Str_2_Loc:           ");
  printstring(Str_2_Loc);
  printnewline();
  printstring("        should be:   DHRYSTONE PROGRAM, 2'ND STRING\n");
  printnewline();

  User_Time = End_Time - Begin_Time;

  if (User_Time == Too_Small_Time)
  {
    printstring("Measured time too small to obtain meaningful results\n");
    printstring("Please increase number of runs\n");
    printnewline();
  }
  else
  {
#ifdef TIMES
    Microseconds = (float) User_Time
                        / (float) Number_Of_Runs;
    Dhrystones_Per_Second = (float) Number_Of_Runs / ((float) User_Time / 1000000);
#else
    Microseconds = (float) User_Time
                        / ((float) HZ * ((float) Number_Of_Runs));
    Dhrystones_Per_Second = ((float) HZ * (float) Number_Of_Runs)
                        / (float) User_Time;
#endif
    printstring("Microseconds for one run through Dhrystone: ");
    printnum(Microseconds);
    printnewline();
    printstring("Dhrystones per Second:                      ");
    printnum(Dhrystones_Per_Second);
    printnewline();
  }
  
}


Proc_1 (Ptr_Val_Par)
/******************/

REG Rec_Pointer Ptr_Val_Par;
    /* executed once */
{
  REG Rec_Pointer Next_Record = Ptr_Val_Par->Ptr_Comp;  
                                        /* == Ptr_Glob_Next */
  /* Local variable, initialized with Ptr_Val_Par->Ptr_Comp,    */
  /* corresponds to "rename" in Ada, "with" in Pascal           */
  
  structassign (*Ptr_Val_Par->Ptr_Comp, *Ptr_Glob); 
  Ptr_Val_Par->variant.var_1.Int_Comp = 5;
  Next_Record->variant.var_1.Int_Comp 
        = Ptr_Val_Par->variant.var_1.Int_Comp;
  Next_Record->Ptr_Comp = Ptr_Val_Par->Ptr_Comp;
  Proc_3 (&Next_Record->Ptr_Comp);
    /* Ptr_Val_Par->Ptr_Comp->Ptr_Comp 
                        == Ptr_Glob->Ptr_Comp */
  if (Next_Record->Discr == Ident_1)
    /* then, executed */
  {
    Next_Record->variant.var_1.Int_Comp = 6;
    Proc_6 (Ptr_Val_Par->variant.var_1.Enum_Comp, 
           &Next_Record->variant.var_1.Enum_Comp);
    Next_Record->Ptr_Comp = Ptr_Glob->Ptr_Comp;
    Proc_7 (Next_Record->variant.var_1.Int_Comp, 10, 
           &Next_Record->variant.var_1.Int_Comp);
  }
  else /* not executed */
    structassign (*Ptr_Val_Par, *Ptr_Val_Par->Ptr_Comp);
} /* Proc_1 */


Proc_2 (Int_Par_Ref)
/******************/
    /* executed once */
    /* *Int_Par_Ref == 1, becomes 4 */

One_Fifty   *Int_Par_Ref;
{
  One_Fifty  Int_Loc;  
  Enumeration   Enum_Loc;

  Int_Loc = *Int_Par_Ref + 10;
  do /* executed once */
    if (Ch_1_Glob == 'A')
      /* then, executed */
    {
      Int_Loc -= 1;
      *Int_Par_Ref = Int_Loc - Int_Glob;
      Enum_Loc = Ident_1;
    } /* if */
  while (Enum_Loc != Ident_1); /* true */
} /* Proc_2 */


Proc_3 (Ptr_Ref_Par)
/******************/
    /* executed once */
    /* Ptr_Ref_Par becomes Ptr_Glob */

Rec_Pointer *Ptr_Ref_Par;

{
  if (Ptr_Glob != Null)
    /* then, executed */
    *Ptr_Ref_Par = Ptr_Glob->Ptr_Comp;
  Proc_7 (10, Int_Glob, &Ptr_Glob->variant.var_1.Int_Comp);
} /* Proc_3 */


Proc_4 () /* without parameters */
/*******/
    /* executed once */
{
  Boolean Bool_Loc;

  Bool_Loc = Ch_1_Glob == 'A';
  Bool_Glob = Bool_Loc | Bool_Glob;
  Ch_2_Glob = 'B';
} /* Proc_4 */


Proc_5 () /* without parameters */
/*******/
    /* executed once */
{
  Ch_1_Glob = 'A';
  Bool_Glob = false;
} /* Proc_5 */


        /* Procedure for the assignment of structures,          */
        /* if the C compiler doesn't support this feature       */
memcpy (d, s, l)
register char   *d;
register char   *s;
register int    l;
{
        while (l--) *d++ = *s++;
}


