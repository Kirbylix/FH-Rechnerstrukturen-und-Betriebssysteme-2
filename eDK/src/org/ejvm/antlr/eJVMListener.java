package org.ejvm.antlr;
// Generated from eJVM.g4 by ANTLR 4.0
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.Token;

public interface eJVMListener extends ParseTreeListener {
	void enterDecimalLiteral(eJVMParser.DecimalLiteralContext ctx);
	void exitDecimalLiteral(eJVMParser.DecimalLiteralContext ctx);

	void enterErrorMessageDefinition(eJVMParser.ErrorMessageDefinitionContext ctx);
	void exitErrorMessageDefinition(eJVMParser.ErrorMessageDefinitionContext ctx);

	void enterCodeLine(eJVMParser.CodeLineContext ctx);
	void exitCodeLine(eJVMParser.CodeLineContext ctx);

	void enterArgument(eJVMParser.ArgumentContext ctx);
	void exitArgument(eJVMParser.ArgumentContext ctx);

	void enterString(eJVMParser.StringContext ctx);
	void exitString(eJVMParser.StringContext ctx);

	void enterProgram(eJVMParser.ProgramContext ctx);
	void exitProgram(eJVMParser.ProgramContext ctx);

	void enterMethodCode(eJVMParser.MethodCodeContext ctx);
	void exitMethodCode(eJVMParser.MethodCodeContext ctx);

	void enterNewline(eJVMParser.NewlineContext ctx);
	void exitNewline(eJVMParser.NewlineContext ctx);

	void enterConstantDefinition(eJVMParser.ConstantDefinitionContext ctx);
	void exitConstantDefinition(eJVMParser.ConstantDefinitionContext ctx);

	void enterAll_char(eJVMParser.All_charContext ctx);
	void exitAll_char(eJVMParser.All_charContext ctx);

	void enterProgramName(eJVMParser.ProgramNameContext ctx);
	void exitProgramName(eJVMParser.ProgramNameContext ctx);

	void enterProgramDeclaration(eJVMParser.ProgramDeclarationContext ctx);
	void exitProgramDeclaration(eJVMParser.ProgramDeclarationContext ctx);

	void enterErrorMessageTable(eJVMParser.ErrorMessageTableContext ctx);
	void exitErrorMessageTable(eJVMParser.ErrorMessageTableContext ctx);

	void enterIdentifier(eJVMParser.IdentifierContext ctx);
	void exitIdentifier(eJVMParser.IdentifierContext ctx);

	void enterMethod(eJVMParser.MethodContext ctx);
	void exitMethod(eJVMParser.MethodContext ctx);

	void enterLocalVariablesSection(eJVMParser.LocalVariablesSectionContext ctx);
	void exitLocalVariablesSection(eJVMParser.LocalVariablesSectionContext ctx);

	void enterLabel(eJVMParser.LabelContext ctx);
	void exitLabel(eJVMParser.LabelContext ctx);

	void enterConstantsSection(eJVMParser.ConstantsSectionContext ctx);
	void exitConstantsSection(eJVMParser.ConstantsSectionContext ctx);

	void enterParameterListEnd(eJVMParser.ParameterListEndContext ctx);
	void exitParameterListEnd(eJVMParser.ParameterListEndContext ctx);

	void enterHexLiteral(eJVMParser.HexLiteralContext ctx);
	void exitHexLiteral(eJVMParser.HexLiteralContext ctx);

	void enterLocalVariableDefinition(eJVMParser.LocalVariableDefinitionContext ctx);
	void exitLocalVariableDefinition(eJVMParser.LocalVariableDefinitionContext ctx);

	void enterInstruction(eJVMParser.InstructionContext ctx);
	void exitInstruction(eJVMParser.InstructionContext ctx);

	void enterEmptyLine(eJVMParser.EmptyLineContext ctx);
	void exitEmptyLine(eJVMParser.EmptyLineContext ctx);

	void enterParameterList(eJVMParser.ParameterListContext ctx);
	void exitParameterList(eJVMParser.ParameterListContext ctx);

	void enterComment(eJVMParser.CommentContext ctx);
	void exitComment(eJVMParser.CommentContext ctx);

	void enterNumberLiteral(eJVMParser.NumberLiteralContext ctx);
	void exitNumberLiteral(eJVMParser.NumberLiteralContext ctx);
}