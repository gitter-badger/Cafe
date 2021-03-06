/*
 * Copyright (c) 2021. Dhyey Shah, Saurabh Pethani, Romil Nisar
 *
 * Developed by:
 *         Dhyey Shah<dhyeyshah4@gmail.com>
 *         https://github.com/dhyey-shah
 *
 * Contributors:
 *         Saurabh Pethani<spethani28@gmail.com>
 *         https://github.com/SaurabhPethani
 *
 *         Romil Nisar<rnisar7@gmail.com>
 *
 *
 * This file is part of Cafe.
 *
 * Cafe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3 of the License.
 *
 * Cafe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cafe.  If not, see <https://www.gnu.org/licenses/>.
 */

package compiler.parser;

import compiler.parser.Tokens.*;
import compiler.util.Log;
import compiler.util.Position;

import java.util.ArrayList;
import java.util.List;

import static compiler.util.Log.Type.*;
import static compiler.util.Messages.message;

public class Tokenizer {

    private Log log;
    private Tokens tokens;
    private CharReader reader;

    private TokenKind tokenKind;

    // Temporary variable to hold value for literals and identifiers. To be changed.
    private String litValue;

    private List<String> comment = null;

    protected Tokenizer(ScannerFactory scannerFactory, List<Character> buff) {
        this(scannerFactory, new CharReader(buff));
    }

    protected Tokenizer(ScannerFactory scannerFactory, CharReader reader) {
        log = scannerFactory.log;
        tokens = scannerFactory.tokens;
        this.reader = reader;
    }

    void scanIden() {
        reader.putChar(reader.ch);
        reader.scanChar();
        boolean isIden = true;
        while (true) {
            switch (reader.ch) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '$':
                case '_':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    reader.putChar(reader.ch);
                    reader.scanChar();
                    continue;
                default:
                    isIden = false;
            }

            if (!isIden) {
                litValue = reader.getSavedBufferAsString(true);
                tokenKind = tokens.lookupKind(litValue);
                return;
            }
        }
    }

    void scanNum() {
        reader.putChar(reader.ch);
        boolean isNum = true;

        while (true) {
            reader.scanChar();
            switch (reader.ch) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    reader.putChar(reader.ch);
                    continue;
                default:
                    isNum = false;
            }

            if (!isNum) {
                litValue = reader.getSavedBufferAsString(true);

                // eg: 5a , 555abcd
                if (Character.isLetter(reader.ch)) {
                    logError(INVALID_IDENTIFIER,
                            message(INVALID_IDENTIFIER, litValue + reader.ch));
                    return;
                }
                tokenKind = TokenKind.NUMLIT;
                return;
            }
        }
    }

    void scanComment() {
        reader.scanChar();
        comment = new ArrayList<>();
        while (true) {
            if (reader.ch == '*') {
                reader.scanChar();
                if (reader.ch == '/') {
                    reader.scanChar();
                    return;
                }
                continue;
            }
            reader.putChar(reader.ch);
            if (reader.ch == ' ' || reader.ch == '\t' || reader.ch == '\n' || reader.ch == '\r') {
                String word = reader.getSavedBufferAsString(true);
                comment.add(word);
            } else if (reader.ch == '\0') {
                logError(EOF_PARSING_COMMENT,
                        message(EOF_PARSING_COMMENT));
                return;
            }
            reader.scanChar();
        }
    }

    void scanString() {
        if (reader.ch == '"') {
            reader.scanChar();
            while (reader.ch != '"' && reader.ch != Character.MIN_VALUE) {
                // Escape char after '\'
                if (reader.ch == '\\') {
                    reader.scanChar();
                    switch (reader.ch) {
                        case 't':
                            reader.putChar('\t');
                            break;
                        case 'n':
                            reader.putChar('\n');
                            break;
                        case 'r':
                            reader.putChar('\r');
                            break;
                    }
                } else
                    reader.putChar(reader.ch);
                reader.scanChar();
            }
            if (reader.ch == Character.MIN_VALUE) {
                logError(EOF,
                        message(EOF));
                return;
            }
            // scan ending DQUOTE ('"')
            reader.scanChar();
            litValue = reader.getSavedBufferAsString(true);
            tokenKind = TokenKind.STRLIT;
            return;
        }
    }

    Token readToken() {
        int startPos = reader.column;
        int lineNumber = reader.lineNumber;
        LOOP:
        while (true) {
            switch (reader.ch) {
                // Ignore white spaces
                case ' ':
                case '\t':
                    do {
                        reader.scanChar();
                    } while (reader.ch == ' ' || reader.ch == '\t');
                    break;
                case '\r':
                case '\n':
                    do {
                        reader.scanChar();
                    } while (reader.ch == '\r' || reader.ch == '\n');
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '$':
                case '_':
                    scanIden();
                    break LOOP;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    scanNum();
                    break LOOP;
                case '.':
                    reader.scanChar();
                    if (reader.ch == '.') {
                        reader.scanChar();
                        if (reader.ch == '.') {
                            reader.scanChar();
                            tokenKind = TokenKind.VARARGS;
                            break LOOP;
                        } else {
                            tokenKind = TokenKind.RANGE;
                            break LOOP;
                        }
                    }
                    tokenKind = TokenKind.DOT;
                    break LOOP;
                case ',':
                    reader.scanChar();
                    tokenKind = TokenKind.COMMA;
                    break LOOP;
                case ':':
                    reader.scanChar();
                    tokenKind = TokenKind.COLON;
                    break LOOP;
                case ';':
                    reader.scanChar();
                    tokenKind = TokenKind.SEMICOLON;
                    break LOOP;
                case '(':
                    reader.scanChar();
                    tokenKind = TokenKind.LPAREN;
                    break LOOP;
                case ')':
                    reader.scanChar();
                    tokenKind = TokenKind.RPAREN;
                    break LOOP;
                case '[':
                    reader.scanChar();
                    tokenKind = TokenKind.LSQU;
                    break LOOP;
                case ']':
                    reader.scanChar();
                    tokenKind = TokenKind.RSQU;
                    break LOOP;
                case '{':
                    reader.scanChar();
                    tokenKind = TokenKind.LCURLY;
                    break LOOP;
                case '}':
                    reader.scanChar();
                    tokenKind = TokenKind.RCURLY;
                    break LOOP;
                case '>':
                    //>=
                    //>>
                    //>>>
                    reader.scanChar();
                    switch (reader.ch) {
                        case '=':
                            reader.scanChar();
                            tokenKind = TokenKind.GTE;
                            break LOOP;
                        case '>':
                            reader.scanChar();
                            if (reader.ch == '>') {
                                reader.scanChar();
                                tokenKind = TokenKind.TRSHIFT;
                                break LOOP;
                            } else {
                                tokenKind = TokenKind.RSHIFT;
                                break LOOP;
                            }
                        default:
                            tokenKind = TokenKind.GT;
                            break LOOP;
                    }
                case '<':
                    //<=
                    //<<
                    reader.scanChar();
                    switch (reader.ch) {
                        case '=':
                            reader.scanChar();
                            tokenKind = TokenKind.LTE;
                            break LOOP;
                        case '<':
                            reader.scanChar();
                            tokenKind = TokenKind.LSHIFT;
                            break LOOP;
                        default:
                            tokenKind = TokenKind.LT;
                            break LOOP;
                    }

                case '!':
                    //!=
                    reader.scanChar();
                    if (reader.ch == '=') {
                        reader.scanChar();
                        tokenKind = TokenKind.NOTEQU;
                        break LOOP;
                    } else {
                        tokenKind = TokenKind.NOTOP;
                        break LOOP;
                    }
                case '=':
                    //==
                    reader.scanChar();
                    if (reader.ch == '=') {
                        reader.scanChar();
                        tokenKind = TokenKind.EQUEQU;
                        break LOOP;
                    } else {
                        tokenKind = TokenKind.EQU;
                        break LOOP;
                    }
                case '&':
                    reader.scanChar();
                    if (reader.ch == '&') {
                        reader.scanChar();
                        tokenKind = TokenKind.ANDOP;
                        break LOOP;
                    } else {
                        tokenKind = TokenKind.BITAND;
                        break LOOP;
                    }
                case '|':
                    reader.scanChar();
                    if (reader.ch == '|') {
                        reader.scanChar();
                        tokenKind = TokenKind.OROP;
                        break LOOP;
                    } else {
                        tokenKind = TokenKind.BITOR;
                        break LOOP;
                    }
                case '^':
                    reader.scanChar();
                    tokenKind = TokenKind.BITAND;
                    break LOOP;
                case '+':
                    reader.scanChar();
                    tokenKind = TokenKind.ADD;
                    break LOOP;
                case '-':
                    reader.scanChar();
                    tokenKind = TokenKind.SUB;
                    break LOOP;
                case '*':
                    // **
                    reader.scanChar();
                    if (reader.ch == '*') {
                        reader.scanChar();
                        tokenKind = TokenKind.POWER;
                        break LOOP;
                    } else {
                        tokenKind = TokenKind.MUL;
                        break LOOP;
                    }
                case '/':
                    // "//"
                    // "/*"
                    reader.scanChar();
                    if (reader.ch == '/') {
                        reader.scanChar();
                        tokenKind = TokenKind.FLOORDIV;
                        break LOOP;
                    } else if (reader.ch == '*') {
                        // TODO: Comments not supported in parsing, so tokens are ignored
                        scanComment();
                        //tokenKind=TokenKind.MULTICOMMENT;
                        break;
                    } else {
                        tokenKind = TokenKind.DIV;
                        break LOOP;
                    }
                case '~':
                    reader.scanChar();
                    tokenKind = TokenKind.TILDE;
                    break LOOP;
                case '#':
                    reader.scanChar();
                    comment = new ArrayList<>();
                    while (reader.ch != Character.MIN_VALUE) {
                        reader.putChar(reader.ch);
                        if (reader.ch == '\r' || reader.ch == '\n') {
                            comment.add(reader.getSavedBufferAsString(true));
                            break;
                        }
                        reader.scanChar();
                    }

                    //tokenKind=TokenKind.SINGLECOMMENT;
                    break;
                case '@':
                    reader.scanChar();
                    tokenKind = TokenKind.IMPORT;
                    break LOOP;
                case '%':
                    reader.scanChar();
                    tokenKind = TokenKind.MOD;
                    break LOOP;
//                case '\'':
//                    reader.scanChar();
//                    tokenKind = TokenKind.SQOUTE;
//                    break LOOP;
                case '"':
                    scanString();
                    break LOOP;
                case '\0':
                    tokenKind = TokenKind.END;
                    break LOOP;
                default:
                    logError(ILLEGAL_CHARACTER,
                            message(ILLEGAL_CHARACTER, reader.ch));
                    break LOOP;
            }
        }
        int endPos = reader.column;
        int endLine = reader.lineNumber;
        Position p = Position.of(startPos, lineNumber, endPos, endLine);

        switch (tokenKind.tag) {
            case DEFAULT:
                String c = null;
                if (comment != null) {
                    c = String.join(" ", comment);
                    comment = null;
                }
                return new Token(tokenKind, p, c);
            case NAMED:
                return new NamedToken(tokenKind, litValue, p, null);
            case STRING:
                return new StringToken(tokenKind, litValue, p, null);
            case NUMERIC:
                return new NumericToken(tokenKind, litValue, p, null);
            default:
                throw new AssertionError();
        }
    }

    private String errorDescription(Position position, String message) {
        return message + ' ' + message(SOURCE_POSITION, position.getStartLine(), position.getStartColumn());
    }

    private void logError(Log.Type issue, String message) {
        Position pos = Position.of(reader.column, reader.lineNumber);
        log.report(issue, pos,
                errorDescription(pos, message));
        tokenKind = TokenKind.ERROR;
    }
}
