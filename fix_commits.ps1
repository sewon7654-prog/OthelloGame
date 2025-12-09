$foundFile = Get-ChildItem -Path "$env:USERPROFILE\Documents" -Filter "GameView.java" -Recurse -ErrorAction SilentlyContinue | Where-Object { $_.DirectoryName -like "*OthelloGam-front-main*" } | Select-Object -First 1
if ($foundFile) {
    $projectRoot = $foundFile.Directory.Parent.Parent.Parent.Parent.Parent.Parent
    Push-Location $projectRoot.FullName
    
    # Start rebase
    $env:GIT_EDITOR = "cmd /c exit 0"
    git rebase -i 71f36f5
    
    # Wait for rebase to start
    Start-Sleep -Seconds 1
    
    # Check if rebase started
    if (Test-Path ".git/rebase-merge/git-rebase-todo") {
        # Create new todo file
        $todoContent = @"
reword ffe24ed Fix chance card success: opponent random move feature
reword 258a8ff Add all project files
pick fa1395f Fix chance card success: opponent random move feature
pick c47a37b Update commit messages to English
"@
        $todoContent | Out-File -FilePath ".git/rebase-merge/git-rebase-todo" -Encoding ASCII -NoNewline
        
        # Set editor for commit messages
        $env:GIT_EDITOR = "powershell -Command `"`$msg = 'Fix chance card success: opponent random move feature'; if (`$args[0] -like '*258a8ff*') { `$msg = 'Add all project files' }; Set-Content -Path `$args[0] -Value `$msg`""
        
        # Continue rebase
        git rebase --continue
        
        # Second commit message
        $env:GIT_EDITOR = "powershell -Command `"Set-Content -Path `$args[0] -Value 'Add all project files'`""
        git rebase --continue
        
        # Push
        git push origin main --force
    }
    
    Pop-Location
}

