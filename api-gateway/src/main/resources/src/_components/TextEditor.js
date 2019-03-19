import React, { Component } from 'react'
import PropTypes from 'prop-types'
import RichTextEditor from 'react-rte'
import { ContentBlock, EditorState } from 'draft-js'
import { Dropdown } from 'semantic-ui-react'

class TextEditor extends Component {
    static propTypes = {
        onChange: PropTypes.func.isRequired,
        text: PropTypes.string.isRequired,
        isSet: PropTypes.bool,
        format: PropTypes.string,
        template: PropTypes.array,
        placeholdersList: PropTypes.array
    }
    static defaultProps = {
        isSet: true,
        format: 'markdown'
    }
    state = {
        value: RichTextEditor.createEmptyValue(),
        isSet: false,
        template: [],
        suggestion: false
    }
    componentDidMount() {
        if(this.props.text) {
            let newValue = RichTextEditor.createValueFromString(this.props.text, this.props.format)
            this.setState({ value: newValue, isSet: true })
        }
        if (this.props.placeholdersList !== null) {
            const template = this._handlePlaceholders(this.props.placeholdersList)
            this.setState({template})
        }

    }
	_handlePlaceholders = (placeholdersList) => {
	    return placeholdersList.map((placeholder) => {
	        return {text:`${placeholder.title} - ${placeholder.description}`, value: placeholder.title}
	    })
	}
	componentWillReceiveProps(nextProps) {
	    if (!this.state.isSet && nextProps.text) {
	        let newValue = RichTextEditor.createValueFromString(nextProps.text, this.props.format)
	        this.setState({ value: newValue, isSet: true })
	    }
	    if (this.props.placeholdersList !== null) {
	        const template = this._handlePlaceholders(this.props.placeholdersList)
	        this.setState({template})
	    }
	}
	componentDidUpdate(prevProps, prevState) {
	    const { text, isSet } = this.props
	    if(text && text !== prevProps.text && (!this.state.isSet || !isSet)) {
	        let newValue = RichTextEditor.createValueFromString(text, this.props.format)
	        this.setState({ value: newValue, isSet: true })
	    }
	}

    onChange = (value) => {
        this.setState({ value })
        if (this.props.onChange) {
            this.props.onChange(value.toString(this.props.format))
        }
    }


	insertValueAtPosition = (editorState, blockKey, start, end, value, valueLength, newType = 'unstyled') => {
	    const content = editorState.getCurrentContent()
	    const blockMap = content.getBlockMap()
	    const block = blockMap.get(blockKey)
	    /** Split the blocks
		 * Cf Drafjs doc  https://draftjs.org/docs/api-reference-content-state#getblockmap and
		 * https://github.com/sstur/react-rte/blob/master/src/lib/insertBlockAfter.js */
	    const blocksBefore = blockMap.toSeq().takeUntil((v) => (v === block))
	    const blocksAfter = blockMap.toSeq().skipUntil((v) => (v === block)).rest()

	    const newValue = block.getText().substring(0, start) + value + block.getText().substring(end + valueLength)
	    const newBlock = new ContentBlock({
		  key: blockKey,
		  type: newType,
		  text: newValue,
		  characterList: block.getCharacterList().slice(0, 0),
		  depth: 0,
	    })
	    const newBlockMap = blocksBefore.concat(
		  [[blockKey, newBlock]],
		  blocksAfter,
	    ).toOrderedMap()
	    const selection = editorState.getSelection()
	    const newContent = content.merge({
		  blockMap: newBlockMap,
		  selectionBefore: selection,
		  selectionAfter: selection.merge({
	            anchorKey: blockKey,
	            anchorOffset: 0,
	            focusKey: blockKey,
	            focusOffset: 0,
	            isBackward: false,
		  }),
	    })
	    return EditorState.push(editorState, newContent, 'split-block')
	  }

	handleClick = (value) => {
	    const editorState = this.state.value.getEditorState()
	    const selection = editorState.getSelection()
	    const blockKey = selection.getStartKey()
	    const start = selection.getStartOffset()
	    const end = selection.getEndOffset()
	    //length of value + "{{}}"
	    const valueLength = value.length + 4

	    const newEditorState = this.insertValueAtPosition(
	        editorState,
	        blockKey,
	        start,
	        end,
	        `{{${value}}}`,
	        valueLength
	    )
	    const newValue = this.state.value.setEditorState(newEditorState)
	    const newValueString = newValue.toString(this.props.format)
	    this.setState({value: RichTextEditor.createValueFromString(newValueString, this.props.format), suggestion:false})
	    this.props.onChange(newValueString)
	}

	render() {

	    let customControls = []
	    if(this.state.template) {
	        /*customControls takes React Nodes array*/
	        const placeholderTool =
				<div style={{display: 'inline-block'}}>
				    <Dropdown icon='tag' className='IconButton__root___3tqZW Button__root___1gz0c'>
				        <Dropdown.Menu>
				            <Dropdown.Menu scrolling>
				                {this.state.template.map(option => (
				                    <Dropdown.Item key={option.value} {...option} onClick={(e, {value}) => this.handleClick(value)}/>
				                ))}
				            </Dropdown.Menu>
				        </Dropdown.Menu>
				    </Dropdown>
				</div>

	        customControls = [
	            placeholderTool
	        ]
	    }

	    return (
	        <div className='editor' style={{position: 'relative'}}>
	            <RichTextEditor
	                customControls={customControls}
	                value={this.state.value}
	                onChange={this.onChange} />
	        </div>
	    )
	}
}

export default TextEditor
